package com.deiconnect.payequity.entity;

import com.deiconnect.common.entity.BaseAuditEntity;
import com.deiconnect.iam.entity.User;
import com.deiconnect.payequity.enums.AnalysisStatus;
import com.deiconnect.payequity.enums.ControlVariable;
import com.deiconnect.payequity.enums.PayDimension;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "pay_equity_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PayEquityAnalysis extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "analysis_period", nullable = false, length = 80)
    private String analysisPeriod;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private PayDimension dimension;

    @ElementCollection(targetClass = ControlVariable.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "pay_equity_control_variables", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "control_variable", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Builder.Default
    private Set<ControlVariable> controlVariables = new HashSet<>();

    @Column(name = "median_gap_percent")
    private Double medianGapPercent;

    @Column(name = "adjusted_gap_percent")
    private Double adjustedGapPercent;

    @Column(name = "significance_level")
    private Double significanceLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_by_user_id")
    private User runBy;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private AnalysisStatus status;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PayGapFlag> flags = new ArrayList<>();
}
