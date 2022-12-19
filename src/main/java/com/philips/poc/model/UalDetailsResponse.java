package com.philips.poc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.philips.poc.entity.UalDetails;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UalDetailsResponse {
    private List<UalDetails> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
