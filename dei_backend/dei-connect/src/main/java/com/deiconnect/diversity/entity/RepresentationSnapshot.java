package com.deiconnect.diversity.entity;

import com.deiconnect.common.entity.BaseAuditEntity;
import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.diversity.enums.SnapshotStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "representation_snapshot", indexes = {
        @Index(name = "idx_repr_snapshot_dimension", columnList = "dimension"),
        @Index(name = "idx_repr_snapshot_dept", columnList = "department_id"),
        @Index(name = "idx_repr_snapshot_status", columnList = "status"),
        @Index(name = "idx_repr_snapshot_run", columnList = "snapshot_date, dimension, department_id, creator_manager_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class RepresentationSnapshot extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "department_id")
    private Long departmentId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private DemographicDimension dimension;

    @Column(name = "group_name", nullable = false, length = 120)
    private String groupName;

    @Column(name = "head_count")
    private Integer count;

    @Column
    private Double percentage;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private SnapshotStatus status;

    @Column(name = "suppressed_group_count")
    private Integer suppressedGroupCount;

    @Column(name = "total_considered")
    private Integer totalConsidered;

    @Column(name = "creator_manager_id")
    private Long creatorManagerId;
}
