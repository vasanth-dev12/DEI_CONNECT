package com.deiconnect.goal.service;

import com.deiconnect.goal.dto.CreateProgressRequest;
import com.deiconnect.goal.dto.ProgressResponse;
import com.deiconnect.goal.dto.UpdateProgressRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GoalProgressService {

    ProgressResponse create(Long goalId, CreateProgressRequest request);

    ProgressResponse update(Long goalId, Long progressId, UpdateProgressRequest request);

    ProgressResponse confirm(Long goalId, Long progressId);

    Page<ProgressResponse> list(Long goalId, Pageable pageable);
}
