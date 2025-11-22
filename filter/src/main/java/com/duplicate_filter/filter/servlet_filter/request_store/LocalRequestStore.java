package com.duplicate_filter.filter.servlet_filter.request_store;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("localRequestStore") //bean name을 파라미터로 넣는데, spring bean의 작명 컨벤션은 카멜 케이스임
public class LocalRequestStore implements RequestStore{

    private ConcurrentHashMap<String, LocalDateTime> hashMap=new ConcurrentHashMap<>();

    @Value("${request.validity.period}")
    private long validityPeriod; 

    @Override
    public boolean storeIfNotDuplicate(String key){
        LocalDateTime now=LocalDateTime.now();
        LocalDateTime result=hashMap.putIfAbsent(key, now); //key가 중복이라면 key에 대한 기존 value, 중복이 아니라면 null 반환 
        if(result==null) return false;
        else if(checkAndUpdateExpiration(key)) return false; //중복인 경우에도 기존 요청이 만료된 경우 "중복이 아닌 것으로" 간주
        else return true;
    }

    @Override
    public boolean checkAndUpdateExpiration(String key){
        //요청 해시(hash)의 저장 시각(value)에 대해 다음을 검사함
        //  -(현재 시각) > (저장 시각 + 유효 기간)

        //위 조건을 만족하는 요청은 만료된 것으로 간주되며
        //더 이상 중복 검사에 필요하지 않으므로 삭제해야함

        LocalDateTime now=LocalDateTime.now();
        LocalDateTime storedTime=hashMap.get(key);
        LocalDateTime expiredTime=storedTime.plusSeconds(validityPeriod);
        boolean expired=now.isAfter(expiredTime);

        hashMap.put(key,now);
        
        return expired;
    }

    //생각해보니 굳이 아래처럼 구현할 필요가 없다고 판단함. 
    //O(n) 시간복잡도 이기 때문에 요청 해시가 많이 저장돼있을 수록 성능상 불리함
    //만약 duplicate일 때 기존의 요청 해시가 만료된 요청이라면 중복이 아닌 것으로 판정하면 됨
    //그러고 난 후 요청 해시에 대한 timeStamp(value)를 갱신하면 O(1) 시간복잡도로도 구현할 수 있음
    /*
    @Override
    @Deprecated
    public int cleanUpExpiredRequests(){ //유효 기간이 지난 요청 해시들을 정리하는 메서드
        //각 요청 해시(hash)의 저장 시각(value)에 대해 다음을 검사함
        // - (현재 시각) > (저장 시각 + 유효 기간)

        //위 조건을 만족하는 요청은 만료된 것으로 간주되며,
        //더 이상 중복 검사에 필요하지 않으므로 삭제해야함

        //이 메서드는 hashMap의 모든 (hash:time) 쌍을 순회하며
        //만료된 항목을 lazy하게 삭제하는 기능을 수행함 

        //순회할 때 .keySet()이 아니라 Iterator<Map.Entry<...>>를 사용하는 이유는 
        //순회 중 다른 스레드가 데이터를 추가하거나 삭제하더라도
        //그 변경 사항이 순회에 반영되지 않기 때문임
        //이 방식은 그러한 경우에서 예외 없이 안전하게 순회할 수 있도록 설계된 fail-safe 방식이며,
        //특히 순회 중 요소를 삭제할 때 안정성을 확보할 수 있음
        //단, 설명했듯이 순회 도중 구조 변경 사항을 반영할 수는 없음

        //return 값으로 삭제된 항목의 수를 반환함

        int deletedCount=0;
        LocalDateTime now=LocalDateTime.now();

        Iterator<Map.Entry<String,LocalDateTime>> iterator=hashMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, LocalDateTime> entry=iterator.next(); 
            
            LocalDateTime storedTime=entry.getValue();
            LocalDateTime expiredTime=storedTime.plusSeconds(validityPeriod);

            boolean expired=now.isAfter(expiredTime);
            if(expired){
                iterator.remove(); 

                deletedCount+=1;
            }
        }
        return deletedCount;
    }
    */

}
