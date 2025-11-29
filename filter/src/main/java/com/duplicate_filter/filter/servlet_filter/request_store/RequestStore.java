package com.duplicate_filter.filter.servlet_filter.request_store;

import java.time.LocalDateTime;

public interface RequestStore {
    
    public boolean storeIfNotDuplicate(String key);

    public boolean isExpired(LocalDateTime v, LocalDateTime now, String key);

}
