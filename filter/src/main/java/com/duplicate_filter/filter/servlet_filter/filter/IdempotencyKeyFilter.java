package com.duplicate_filter.filter.servlet_filter.filter;

import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.duplicate_filter.filter.servlet_filter.exception.MissingIdempotencyKeyException;
import com.duplicate_filter.filter.servlet_filter.request_store.RequestStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("idem")
public class IdempotencyKeyFilter extends OncePerRequestFilter{

    @Autowired
    @Qualifier("localRequestStore")
    private RequestStore store;
    
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        
        String idempotencyKey; //idempotencyKey=null ? 
        try{
            idempotencyKey=requireIdempotencyKey(request);
        }catch(MissingIdempotencyKeyException e){
            rejectRequest(response, 400, e.getMessage());
            return ;
        }
        
        boolean duplicate=store.storeIfNotDuplicate(idempotencyKey);
        if(duplicate){
            log.warn("Duplicate request occured(Idempotency Key)");
            rejectRequest(response);
        }else{
            filterChain.doFilter(request, response);
        }
        return ;
    }

    public String requireIdempotencyKey(HttpServletRequest request){ 
        String idempotencyKey=request.getHeader("Idempotency-Key");
        if(idempotencyKey==null){
            throw new MissingIdempotencyKeyException("Idempotency Key cannot be found in header");
        }
        return idempotencyKey;
    }
    
    public void rejectRequest(HttpServletResponse response) throws IOException{
        response.setStatus(429); //Too Many Requests
        response.setContentType("application/json");
        PrintWriter writer=response.getWriter();
        writer.write("{\"error\": \"Duplicate request detected. Please try again later. (Idempotency Key)\"}");
    }

    public void rejectRequest(HttpServletResponse response, int status, String message) throws IOException{
        response.setStatus(status); //Too Many Requests
        response.setContentType("application/json");
        PrintWriter writer=response.getWriter();
        writer.write("{\"error\": \"" + message + "\"}");
    }


}
