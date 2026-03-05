package com.smarttaskassistant.task.util;

import com.smarttaskassistant.task.model.Task;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class SortUtils {

    private static final Set<String> TASK_FIELDS = Arrays.stream(Task.class.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toSet());

    public static Sort buildSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.unsorted();
        }

        return Sort.by(
                Arrays.stream(sort)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(s -> s.split(","))
                        .filter(parts -> TASK_FIELDS.contains(parts[0]))
                        .map(parts -> new Sort.Order(
                                (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                                        ? Sort.Direction.ASC
                                        : Sort.Direction.DESC,
                                parts[0]
                        ))
                        .toList()
        );
    }
}
