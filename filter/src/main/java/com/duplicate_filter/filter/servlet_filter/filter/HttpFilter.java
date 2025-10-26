package com.duplicate_filter.filter.servlet_filter.filter;

import java.io.IOException;
import com.duplicate_filter.filter.servlet_filter.DTO.RequestDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface HttpFilter{
    
    public RequestDetails getRequestDetails(HttpServletRequest request) throws IOException;

    public void denyTheRequest(HttpServletResponse response) throws IOException;

}
