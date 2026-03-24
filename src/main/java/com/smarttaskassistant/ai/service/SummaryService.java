package com.smarttaskassistant.ai.service;

import com.smarttaskassistant.ai.model.Summary;
import com.smarttaskassistant.ai.repository.SummaryRepository;
import com.smarttaskassistant.task.event.TaskCreatedEvent;
import com.smarttaskassistant.task.event.TaskDeletedEvent;
import com.smarttaskassistant.task.event.TaskUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final SummaryRepository summaryRepository;

    public void markTodaySummaryDirty(Long userId) {
        String id = Summary.compositeId(userId, LocalDate.now());
        summaryRepository.findById(id).ifPresent(s -> {
            s.setDirty(true);
            summaryRepository.save(s);
        });
    }

    @EventListener
    public void onTaskCreated(TaskCreatedEvent event) {
        markTodaySummaryDirty(event.userId());
    }

    @EventListener
    public void onTaskUpdated(TaskUpdatedEvent event) {
        markTodaySummaryDirty(event.userId());
    }

    @EventListener
    public void onTaskDeleted(TaskDeletedEvent event) {
        markTodaySummaryDirty(event.userId());
    }
}
