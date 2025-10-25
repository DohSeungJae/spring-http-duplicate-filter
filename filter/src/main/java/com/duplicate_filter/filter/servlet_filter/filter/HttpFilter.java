package com.duplicate_filter.filter.servlet_filter.filter;

import java.io.IOException;
import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;

public interface HttpFilter{
    
    public HashMap<String, String> getRequestDetails(HttpServletRequest request) throws IOException;

}
