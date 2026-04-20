package com.bookms.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardStatsResponse {

    private long totalBooks;

    private long totalReaders;

    private long monthBorrowCount;

    private long pendingApprovalCount;

    private long pendingPickupCount;

    private long overdueCount;

    private List<TrendItem> borrowTrend;

    private List<PopularBookItem> popularBooks;

    private List<StatusItem> borrowStatusDistribution;

    @Getter
    @AllArgsConstructor
    public static class TrendItem {

        private String date;

        private long count;
    }

    @Getter
    @AllArgsConstructor
    public static class PopularBookItem {

        private Long bookId;

        private String title;

        private long borrowCount;
    }

    @Getter
    @AllArgsConstructor
    public static class StatusItem {

        private Integer status;

        private String label;

        private long count;
    }
}