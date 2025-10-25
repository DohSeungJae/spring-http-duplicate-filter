package com.duplicate_filter.filter.servlet_filter.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.duplicate_filter.filter.servlet_filter.cached_body.CachedBodyHttpServletRequest;
import com.duplicate_filter.filter.servlet_filter.request_hasher.RequestHasher;
import com.duplicate_filter.filter.servlet_filter.request_store.RequestStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HashFilter extends OncePerRequestFilter implements HttpFilter {

    @Autowired
    @Qualifier("hashMapRequestStore") //bean name을 파라미터로 넣는데, spring bean의 작명 컨벤션은 카멜 케이스임
    private RequestStore store;

    @Autowired
    private RequestHasher hasher;

    private final ObjectMapper mapper=new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        //request에서 직접 body를 가져오면 Controller에서는 body를 읽을 수 없기 때문에 body를 읽기 전에 미리 cache하도록 함
        CachedBodyHttpServletRequest cachedRequest=new CachedBodyHttpServletRequest(request);

        HashMap<String,String> requestDetails=this.getRequestDetails(cachedRequest);
        String hashedRequest=requestDetails.get("hashedRequest");
        
        Boolean duplicate=store.isDuplicateOrElseStore(hashedRequest);
        if(duplicate){
            response.setStatus(429); //Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Duplicate request detected. Please try again later.\"}"); //Object Mapper 사용하기
            return ;
        }else{
            filterChain.doFilter(cachedRequest, response);
        }
    }

    @Override
    public HashMap<String, String> getRequestDetails(HttpServletRequest request) throws IOException{
        if (!(request instanceof CachedBodyHttpServletRequest)) {
            throw new IllegalArgumentException("Expected CachedBodyHttpServletRequest but received " + request.getClass().getSimpleName());
        }

        HashMap<String, String> hashMap=new HashMap<>();

        String content=getContentFromRequest(request);
        String ipAddress=request.getRemoteAddr(); //body에 접근하지 않기 때문에 body는 소모되지 않음
        String requestURL=request.getRequestURL().toString(); //body에 접근하지 않기 때문에 body는 소모되지 않음 //.getRequestURL()은 StringBuffer를 반환하기 때문에 .toString()이 필요함
        String hashedRequest;
        try{
            hashedRequest=hasher.hashify(content, ipAddress, requestURL);
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException("Algorithm for hashing not correct"); //RuntimeError로 처리하면 안되는데... Custom Exception을 만들어야해..
        } 

        hashMap.put("hashedRequest",hashedRequest);
        return hashMap;
    }

    public String getContentFromRequest(HttpServletRequest cachedRequest) throws IOException{ //사실상 CachedBodyHttpServletRequest을 받음
        String body;
        //try-with-resource, reader 사용이 끝나면 JVM이 reader.close()를 자동으로 호출 <- 리소스 누수 방지
        try(BufferedReader reader=new BufferedReader(cachedRequest.getReader())){
            body=reader.lines().collect(Collectors.joining("\n"));
        }
        JsonNode jsonNode=mapper.readTree(body);
        String value=jsonNode.get("string").asText();
        
        return value;
    }


    
}
