package com.smarttaskassistant.task.spec;

import com.smarttaskassistant.task.model.Task;
import com.smarttaskassistant.task.model.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TaskSpecification {

    public static Specification<Task> hasUserId(Long userId) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"), userId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasSeverity(Integer severity) {
        return (root, query, cb) ->
                severity == null ? null : cb.equal(root.get("severity"), severity);
    }

    public static Specification<Task> dueBefore(LocalDateTime dueBefore) {
        return (root, query, cb) ->
                dueBefore == null ? null : cb.lessThanOrEqualTo(root.get("dueTime"), dueBefore);
    }

    public static Specification<Task> dueAfter(LocalDateTime dueAfter) {
        return (root, query, cb) ->
                dueAfter == null ? null : cb.greaterThanOrEqualTo(root.get("dueTime"), dueAfter);
    }

}
