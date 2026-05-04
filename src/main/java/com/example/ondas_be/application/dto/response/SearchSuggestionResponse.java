package com.example.ondas_be.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionResponse {

    /** Các từ khóa user đã tìm gần đây (tối đa 10) */
    private List<String> recentSearches;

    /** Các từ khóa phổ biến nhất toàn hệ thống trong 7 ngày qua (tối đa 10) */
    private List<String> trendingSearches;

    /** Top bài hát theo lượt nghe */
    private List<SongResponse> trendingSongs;

    /** Tất cả thể loại — để hiển thị dạng Browse by Genre */
    private List<GenreResponse> genres;
}
