package com.smarttaskassistant.ai.schedule;

import com.smarttaskassistant.ai.service.VoiceCommandService;
import com.smarttaskassistant.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * At the start of each day (configurable cron), prefetches today's summary for users who were
 * active yesterday (task changes or summary request), so the first API call can hit Mongo cache.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.summary.prefetch.enabled", havingValue = "true", matchIfMissing = true)
public class DailySummaryPrefetchScheduler {

    private final VoiceCommandService voiceCommandService;
    private final UserService userService;

    @Value("${app.summary.activity.zone:}")
    private String configuredZoneId;

    private ZoneId activityZone() {
        if (configuredZoneId == null || configuredZoneId.isBlank()) {
            return ZoneId.systemDefault();
        }
        return ZoneId.of(configuredZoneId);
    }

    @Scheduled(cron = "${app.summary.prefetch.cron:0 5 0 * * *}")
    public void prefetchSummariesForYesterdayActiveUsers() {
        ZoneId zone = activityZone();
        LocalDate yesterday = LocalDate.now(zone).minusDays(1);
        LocalDate today = LocalDate.now(zone);

        var userIds = userService.findUserIdsActiveOn(yesterday);
        log.info("Daily summary prefetch: {} users active on {}, generating for {} if missing",
                userIds.size(), yesterday, today);

        for (Long userId : userIds) {
            try {
                voiceCommandService.prefetchDailySummaryForUser(userId, today);
            } catch (Exception e) {
                log.error("Daily summary prefetch failed for userId={}", userId, e);
            }
        }
    }
}
