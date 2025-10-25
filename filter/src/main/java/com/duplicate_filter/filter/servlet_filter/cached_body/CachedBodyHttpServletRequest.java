package com.duplicate_filter.filter.servlet_filter.cached_body;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.util.StreamUtils;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
    //이 클래스는 기존 객체(HttpServletRequestWrapper)를 감싸서 기능을 확장하는 데코레이터 패턴이라고 함.

    //HttpServletRequestWrapper는 자신이 받은 요청 객체에 대부분의 작업을 넘기는데, 이것을 위임이라고 함.
    //이 클래스는 대부분의 기능을 HttpServletRequest에게 위임.
    //이 클래스도 HttpServletRequest에 대한 데코레이터 클래스라고 볼 수 있음.

    private byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException{
        super(request); 
        //상위 클래스인 HttpServletRequestWrapper의 생성자 호출, request를 내부 필드에 저장함. 
        //request는 상위 클래스인 HttpServletRequestWrapper에 private HttpServletRequest request의 형태로 선언돼있음.
        //하위 클래스는 request 필드를 "가지고" 있기는 하지만 접근 제어자가 private이기 때문에 직접 접근할 수는 없음.

        InputStream requestInputStream=request.getInputStream();
        this.cachedBody=StreamUtils.copyToByteArray(requestInputStream);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(this.cachedBody);
    }
    //원래 HttpServletRequest의 getInputStream()은 요청 본문을 한 번만 읽을 수 있는 스트림을 반환하는데,
    //이 메서드는 cachedBody에 저장된 바이트 배열을 기반으로 새로운 SErvletInputStream을 생성해 반환한다.
    //본문을 여러 번 읽을 수 있도록 캐싱된 데이터를 스트림 형태로 제공하는 것.

    @Override
    public BufferedReader getReader(){
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
    //요청 본문을 문자 기반으로 읽을 수 있는 BufferedReader를 반환
}
