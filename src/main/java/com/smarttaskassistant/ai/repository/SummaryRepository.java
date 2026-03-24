package com.smarttaskassistant.ai.repository;

import com.smarttaskassistant.ai.model.Summary;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SummaryRepository extends MongoRepository<Summary, String> {
}
