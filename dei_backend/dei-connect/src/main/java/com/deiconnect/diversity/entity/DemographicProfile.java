package com.deiconnect.diversity.entity;

import com.deiconnect.common.entity.BaseAuditEntity;
import com.deiconnect.diversity.enums.AgeGroup;
import com.deiconnect.diversity.enums.ConsentStatus;
import com.deiconnect.diversity.enums.DisabilityStatus;
import com.deiconnect.diversity.enums.Ethnicity;
import com.deiconnect.diversity.enums.Gender;
import com.deiconnect.diversity.enums.VeteranStatus;
import com.deiconnect.iam.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "demographic_profile", uniqueConstraints = {
        @UniqueConstraint(name = "uk_demographic_profile_employee", columnNames = "employee_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class DemographicProfile extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_user_id", nullable = false, updatable = false)
    private User employee;

    @Column(name = "employee_id", nullable = false, updatable = false, length = 64)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 40)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 40)
    private Ethnicity ethnicity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private DisabilityStatus disability;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "veteran_status", nullable = false, length = 40)
    private VeteranStatus veteranStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "age_group", nullable = false, length = 40)
    private AgeGroup ageGroup;

    @Column(name = "data_collected_date")
    private LocalDate dataCollectedDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "consent_status", nullable = false, length = 20)
    private ConsentStatus consentStatus;
}
