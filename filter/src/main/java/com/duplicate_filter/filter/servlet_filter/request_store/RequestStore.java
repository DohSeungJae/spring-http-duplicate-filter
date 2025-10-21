package com.duplicate_filter.filter.servlet_filter.request_store;

import java.time.LocalDateTime;

public interface RequestStore {
    
    public boolean isDuplicate(String hash);

    public boolean storeRequest(String hash, LocalDateTime date);

    public int cleanUpExpiredRequests();


}
