package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.request.SearchFilterRequest;
import com.example.ondas_be.application.dto.response.SearchResponse;
import com.example.ondas_be.application.dto.response.SearchSuggestionResponse;

public interface SearchServicePort {

    SearchResponse search(SearchFilterRequest filter);

    SearchSuggestionResponse getSuggestions(String userEmail);

    void saveSearchHistory(String query, String userEmail);

    void clearSearchHistory(String userEmail);
}
