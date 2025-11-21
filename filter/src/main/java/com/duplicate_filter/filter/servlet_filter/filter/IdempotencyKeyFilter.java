package com.duplicate_filter.filter.servlet_filter.filter;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.duplicate_filter.filter.servlet_filter.exception.IdempotencyKeyNotFoundException;
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
    @Qualifier("hashMapRequestStore")
    private RequestStore store;
    
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        
        String idempotencyKey=null;
        try{
            idempotencyKey=getIdempotencyKeyOrElseThrow(request); //Java 스럽게 변경? //requireIdempotencyKey
        }catch(IdempotencyKeyNotFoundException e){
            denyTheRequest(response, 400, e.getMessage());
            return ;
        }
        
        Boolean duplicate=store.isDuplicateOrElseStore(idempotencyKey); //exception으로 바꾸기?, Java스럽게 변경? -> requireNotDuplicateAndStore(exception 사용하는 경우)
        //storeIfNotDuplicate
        if(duplicate){
            log.warn("Duplicate Request Occured(Idempotency Key)");
            denyTheRequest(response);
        }else{
            filterChain.doFilter(request, response);
        }
        return ;
    }


    public void denyTheRequest(HttpServletResponse response) throws IOException{
        response.setStatus(429); //Too Many Requests
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Duplicate request detected. Please try again later. (Idempotency Key)\"}");
    }

    public void denyTheRequest(HttpServletResponse response, int status, String message) throws IOException{
        response.setStatus(status); //Too Many Requests
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    public String getIdempotencyKeyOrElseThrow(HttpServletRequest request) throws IdempotencyKeyNotFoundException{ //MissingIdempotencyKeyException?
        String idempotencyKey=request.getHeader("Idempotency-Key");
        if(idempotencyKey==null){
            throw new IdempotencyKeyNotFoundException("Idempotency Key cannot be found in header");
        }
        return idempotencyKey;
    }
}
