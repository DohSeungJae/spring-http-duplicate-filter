package com.duplicate_filter.filter.servlet_filter.request_store;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @param @Component(bean name), spring bean의 작명 컨벤션은 카멜 케이스
 */
@Component("localRequestStore") 
public class LocalRequestStore implements RequestStore{

    private ConcurrentHashMap<String, LocalDateTime> hashMap=new ConcurrentHashMap<>();

    @Value("${request.validity.period}")
    private long validityPeriod; 


    /**
     * 주어진 key의 저장 시각을 확인하여 중복(만료) 여부 판단 및 갱신 
     * 
     * @param key는 요청을 해시 처리한 값 혹은 Idempotency Key 값
     * @return key가 중복일 때 true 반환
     */ 
    @Override
    public boolean storeIfNotDuplicate(String key){
        AtomicBoolean duplicateFlag=new AtomicBoolean(false);
        hashMap.compute(key, (k,v)->{
            LocalDateTime now=LocalDateTime.now();
            if(v==null){
                return now;
            }else if(isExpired(v,now,key)){
                return now;
            }else{
                duplicateFlag.set(true);
                return now;
            }
        });
        return duplicateFlag.get();
    }

    @Override
    public boolean isExpired(LocalDateTime v, LocalDateTime now, String key){
        LocalDateTime expiredTime=v.plusSeconds(validityPeriod);
        boolean expired=now.isAfter(expiredTime);
        return expired;
    }

}
