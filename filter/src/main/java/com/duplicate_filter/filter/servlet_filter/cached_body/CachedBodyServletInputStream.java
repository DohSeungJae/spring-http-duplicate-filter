package com.duplicate_filter.filter.servlet_filter.cached_body;

import java.io.ByteArrayInputStream;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

public class CachedBodyServletInputStream extends ServletInputStream {
    //이 클래스는 ByteArrayInputStream에게 읽기 작업을 위임하는 구조
    //대부분의 기능을 ByteArrayInputStream가 수행하는데 굳이 이 클래스를 정의한 이유?
    //ServletInputStream을 상속하기 위함.
    //CachedBodyHttpServletRequest 클래스에서 public ServletInputStream getInputStream()를
    //구현(오버라이딩)해야하기 때문  
    
    private final ByteArrayInputStream inputStream;

    public CachedBodyServletInputStream(byte[] cachedBody){
        this.inputStream=new ByteArrayInputStream(cachedBody);
    }

    @Override
    public boolean isFinished(){
        return inputStream.available()==0;
    }

    @Override
    public boolean isReady(){
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener){
        throw new UnsupportedOperationException("Async reading not supported");
    }

    @Override
    public int read(){
        return inputStream.read();
    }
}
