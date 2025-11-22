package com.duplicate_filter.filter.servlet_filter.request_store;

public interface RequestStore {
    
    public boolean storeIfNotDuplicate(String key);

    public boolean checkAndUpdateExpiration(String key);

}
