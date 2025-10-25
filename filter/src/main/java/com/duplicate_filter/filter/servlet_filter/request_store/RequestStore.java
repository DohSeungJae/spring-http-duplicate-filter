package com.duplicate_filter.filter.servlet_filter.request_store;

public interface RequestStore {
    
    public boolean isDuplicateOrElseStore(String key);

    public boolean isRequestExpired(String key);


}
