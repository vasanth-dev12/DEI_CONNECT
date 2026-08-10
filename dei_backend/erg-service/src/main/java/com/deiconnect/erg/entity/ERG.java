package com.deiconnect.erg.entity;

import com.deiconnect.common.entity.BaseAuditEntity;
import com.deiconnect.erg.enums.ErgFocus;
import com.deiconnect.erg.enums.ErgStatus;
import jakarta.persistence.CascadeType;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "erg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ERG extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "erg_name", nullable = false, length = 150)
    private String ergName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ErgFocus focus;

    @Column(length = 1000)
    private String mission;

    @Column(name = "executive_sponsor_id")
    private Long executiveSponsorId;

    @Column(name = "erg_lead_id", nullable = false)
    private Long ergLeadId;

    @Column(name = "member_count", nullable = false)
    private Integer memberCount;

    @Column(name = "founded_date")
    private LocalDate foundedDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ErgStatus status;

    @OneToMany(mappedBy = "erg", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ERGMembership> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "erg", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ERGEvent> events = new ArrayList<>();

    @Column(name = "creator_manager_user_id")
    private Long creatorManagerId;
}
