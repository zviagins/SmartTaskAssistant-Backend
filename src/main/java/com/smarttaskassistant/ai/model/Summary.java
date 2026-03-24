package com.smarttaskassistant.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Daily task summary persisted in MongoDB. Document id is {@code userId + "_" + date} (ISO date).
 */
@Document(collection = "summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Summary {

    @Id
    private String id;

    private Long userId;

    private LocalDate date;

    private String content;

    /**
     * Reserved for cache invalidation when tasks change and the summary should be regenerated.
     */
    private boolean dirty;

    private Instant lastGenerated;

    public static String compositeId(Long userId, LocalDate date) {
        return userId + "_" + date;
    }
}
