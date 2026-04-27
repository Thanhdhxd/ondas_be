package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.request.SearchFilterRequest;
import com.example.ondas_be.application.dto.response.SearchResponse;

public interface SearchServicePort {

    SearchResponse search(SearchFilterRequest filter);
}
