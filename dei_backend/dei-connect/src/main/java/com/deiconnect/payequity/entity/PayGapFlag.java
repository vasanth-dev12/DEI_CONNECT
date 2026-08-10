package com.deiconnect.payequity.entity;

import com.deiconnect.common.entity.BaseAuditEntity;
import com.deiconnect.iam.entity.User;
import com.deiconnect.payequity.enums.FlagStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pay_gap_flag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PayGapFlag extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private PayEquityAnalysis analysis;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "group_name", nullable = false, length = 120)
    private String groupName;

    @Column(name = "gap_percent", nullable = false)
    private Double gapPercent;

    @Column(name = "affected_employee_count", nullable = false)
    private Integer affectedEmployeeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remediation_owner_user_id")
    private User remediationOwner;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private FlagStatus status;
}
