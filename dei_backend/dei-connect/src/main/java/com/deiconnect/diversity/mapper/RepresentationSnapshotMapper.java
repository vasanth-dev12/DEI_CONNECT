package com.deiconnect.diversity.mapper;

import com.deiconnect.diversity.dto.RepresentationSnapshotResponse;
import com.deiconnect.diversity.entity.RepresentationSnapshot;
import com.deiconnect.iam.enums.DepartmentName;
import org.springframework.stereotype.Component;

@Component
public class RepresentationSnapshotMapper {
    public RepresentationSnapshotResponse toResponse(RepresentationSnapshot snapshot, int minGroupSize) {
        boolean suppressed = snapshot.getCount() == null || snapshot.getCount() < minGroupSize;
        return new RepresentationSnapshotResponse(
                snapshot.getId(),
                snapshot.getSnapshotDate(),
                snapshot.getDepartmentId(),
                DepartmentName.fromId(snapshot.getDepartmentId()),
                snapshot.getDimension(),
                snapshot.getGroupName(),
                suppressed ? null : snapshot.getCount(),
                suppressed ? null : snapshot.getPercentage(),
                snapshot.getStatus(),
                suppressed);
    }
}
