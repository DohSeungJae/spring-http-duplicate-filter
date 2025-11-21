package com.duplicate_filter.filter.servlet_filter.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.duplicate_filter.filter.servlet_filter.cached_body.CachedBodyHttpServletRequest;
import com.duplicate_filter.filter.servlet_filter.exception.MissingRequestBodyException;
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
@Profile("hash")
public class HashFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("hashMapRequestStore") 
    private RequestStore store;

    @Autowired
    private RequestHasher hasher;

    private final ObjectMapper mapper=new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        //request에서 직접 body를 가져오면 Controller에서는 body를 읽을 수 없기 때문에 body를 읽기 전에 미리 cache하도록 함
        CachedBodyHttpServletRequest cachedRequest=new CachedBodyHttpServletRequest(request);
        String hashedRequest;
        try{
            hashedRequest=getHashedRequest(cachedRequest);
        }catch(MissingRequestBodyException e){
            denyTheRequest(response, 400, e.getMessage());
            return ;
        }

        boolean duplicate=store.isDuplicateOrElseStore(hashedRequest);
        if(duplicate){
            log.warn("Duplicate request occured");
            denyTheRequest(response);
            return ;
        }else{
            filterChain.doFilter(cachedRequest, response);
        }
    }

    public String getHashedRequest(CachedBodyHttpServletRequest request) throws IOException{
        String content=getContentFromRequest(request);
        String ipAddress=request.getRemoteAddr(); //body에 접근하지 않기 때문에 body는 소모되지 않음
        StringBuffer requestURL=request.getRequestURL(); //body에 접근하지 않기 때문에 body는 소모되지 않음
        String requestURLString=requestURL.toString(); 
        String hashedRequest;
        try{
            hashedRequest=hasher.hashify(content,ipAddress,requestURLString);
        }catch(NoSuchAlgorithmException e){
            throw new IllegalArgumentException("Algorithm for hashing not correct");
        }

        return hashedRequest;
    }

    public String getContentFromRequest(HttpServletRequest cachedRequest) throws IOException{ //사실상 CachedBodyHttpServletRequest을 받음
        String body;
        //try-with-resource, reader 사용이 끝나면 JVM이 reader.close()를 자동으로 호출 <- 리소스 누수 방지
        try(BufferedReader reader=cachedRequest.getReader()){
            Stream<String> lines=reader.lines();
            Collector<CharSequence, ?, String> coll=Collectors.joining("\n");
            body=lines.collect(coll);
        }

        JsonNode jsonNode=mapper.readTree(body);
        JsonNode stringNode=jsonNode.get("string");
        String trimedBody=body.trim();
        if(trimedBody.isEmpty() || stringNode==null){ 
            throw new MissingRequestBodyException("Request body is missing");
        }

        String content=stringNode.asText();
        return content;
    }

    public void denyTheRequest(HttpServletResponse response) throws IOException{
        response.setStatus(429); //Too Many Requests
        response.setContentType("application/json");
        PrintWriter writer=response.getWriter();
        writer.write("{\"error\": \"Duplicate request detected. Please try again later.\"}");
    }

    public void denyTheRequest(HttpServletResponse response, int status, String message) throws IOException{
        response.setStatus(status); 
        response.setContentType("application/json");
        PrintWriter writer=response.getWriter();
        writer.write("{\"error\": \"" + message + "\"}");
    }

    
}
