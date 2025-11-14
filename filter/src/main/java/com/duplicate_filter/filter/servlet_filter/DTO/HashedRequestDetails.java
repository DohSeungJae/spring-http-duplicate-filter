package com.duplicate_filter.filter.servlet_filter.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HashedRequestDetails implements RequestDetails {

    private String hashedRequest;

}
