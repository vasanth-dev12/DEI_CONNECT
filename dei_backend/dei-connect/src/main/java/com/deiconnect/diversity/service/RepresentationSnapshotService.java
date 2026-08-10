package com.deiconnect.diversity.service;

import com.deiconnect.diversity.dto.GenerateSnapshotRequest;
import com.deiconnect.diversity.dto.GenerateSnapshotResult;
import com.deiconnect.diversity.dto.RepresentationSnapshotResponse;
import com.deiconnect.diversity.dto.SnapshotGroupResponse;
import com.deiconnect.diversity.dto.SnapshotRunResponse;
import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.diversity.enums.SnapshotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RepresentationSnapshotService {

    GenerateSnapshotResult generate(GenerateSnapshotRequest payload);

    Page<RepresentationSnapshotResponse> search(DemographicDimension dimension,
                                                Long departmentId,
                                                SnapshotStatus status,
                                                Pageable pageable);

    Page<SnapshotRunResponse> searchRuns(DemographicDimension dimension,
                                         Long departmentId,
                                         SnapshotStatus status,
                                         Pageable pageable);

    RepresentationSnapshotResponse getById(Long snapshotId);

    SnapshotGroupResponse getDistribution(Long snapshotId);

    RepresentationSnapshotResponse publish(Long snapshotId);

    SnapshotRunResponse publishRun(Long snapshotId);

    void delete(Long snapshotId);

    void deleteRun(Long snapshotId);
}