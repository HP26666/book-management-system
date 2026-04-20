package com.bookms.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "bookms")
public class BookmsProperties {

    private Cors cors = new Cors();

    private File file = new File();

    private Borrow borrow = new Borrow();

    @Data
    public static class Cors {

        @NotEmpty
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost", "http://localhost:5173"));
    }

    @Data
    public static class File {

        @NotBlank
        private String uploadDir = "/app/uploads";

        @Min(1)
        private long maxSizeBytes = 5 * 1024 * 1024L;

        @NotEmpty
        private List<String> allowedExtensions = new ArrayList<>(List.of("jpg", "jpeg", "png", "gif"));
    }

    @Data
    public static class Borrow {

        private BigDecimal dailyFineRate = BigDecimal.valueOf(0.5);

        private BigDecimal maxFinePriceRatio = BigDecimal.valueOf(0.5);

        private int renewDays = 15;

        private int renewMaxCount = 1;

        private int reservationExpireDays = 7;

        private int notifyExpireDays = 3;

        private int readerCardValidYears = 3;

        private String mockWechatUsernamePrefix = "wx_mock_";
    }
}