package com.bookms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    private long totalBooks;
    private long onShelfBooks;
    private long totalBorrows;
    private long monthBorrows;
    private long pendingApprovals;
    private long overdueCount;
    private List<HotBookVO> hotBooks;
    private List<Map<String, Object>> monthlyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotBookVO {
        private Long bookId;
        private String title;
        private String author;
        private long borrowCount;
    }
}
