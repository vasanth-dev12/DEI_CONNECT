package com.deiconnect.erg.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.erg.dto.MembershipResponse;
import com.deiconnect.erg.dto.UpdateMembershipRequest;
import com.deiconnect.erg.entity.ERG;
import com.deiconnect.erg.entity.ERGMembership;
import com.deiconnect.erg.enums.ErgStatus;
import com.deiconnect.erg.enums.MembershipRole;
import com.deiconnect.erg.enums.MembershipStatus;
import com.deiconnect.erg.mapper.ErgMembershipMapper;
import com.deiconnect.erg.repository.ErgMembershipRepository;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ErgMembershipServiceImpl implements ErgMembershipService {

    private final ErgMembershipRepository membershipRepository;
    private final ErgService ergService;
    private final ErgMembershipMapper membershipMapper;
    private final AuditLogWriter auditLogWriter;

    @Override
    @Transactional
    public MembershipResponse join(Long ergId) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        ergService.assertEmployeeCanAccess(ergId);
        ERG erg = ergService.findOrThrow(ergId);
        if (erg.getStatus() != ErgStatus.ACTIVE) {
            throw new ConflictException("Cannot join an inactive ERG");
        }

        ERGMembership membership = membershipRepository
                .findByErg_IdAndEmployeeUserId(ergId, principal.getId())
                .orElse(null);

        if (membership != null) {
            if (membership.getStatus() == MembershipStatus.ACTIVE) {
                throw new ConflictException("You are already an active member of this ERG");
            }

            membership.setStatus(MembershipStatus.ACTIVE);
            membership.setJoinDate(LocalDate.now());
        } else {
            membership = ERGMembership.builder()
                    .erg(erg)
                    .employeeUserId(principal.getId())
                    .employeeId(principal.getEmployeeId())
                    .role(MembershipRole.MEMBER)
                    .joinDate(LocalDate.now())
                    .status(MembershipStatus.ACTIVE)
                    .build();
        }

        membership = membershipRepository.save(membership);
        ergService.recomputeMemberCount(ergId);
        auditLogWriter.record(principal.getId(), "JOIN_ERG", "ERGMembership", membership.getId());
        return membershipMapper.toResponse(membership);
    }

    @Override
    @Transactional
    public void leave(Long ergId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ERGMembership membership = membershipRepository.findByErg_IdAndEmployeeUserId(ergId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You are not a member of this ERG"));
        if (membership.getStatus() == MembershipStatus.INACTIVE) {
            throw new ConflictException("You are not an active member of this ERG");
        }
        membership.setStatus(MembershipStatus.INACTIVE);
        membershipRepository.save(membership);
        ergService.recomputeMemberCount(ergId);
        auditLogWriter.record(userId, "LEAVE_ERG", "ERGMembership", membership.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MembershipResponse> list(Long ergId, Pageable pageable) {
        ERG erg = ergService.findOrThrow(ergId);
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        if (principal.getRole() == Role.DEI_MANAGER) {
            if (erg.getCreatorManagerId() == null || !erg.getCreatorManagerId().equals(principal.getId())) {
                throw new ForbiddenOperationException("You may only view memberships of ERG groups you created");
            }
        }
        if (principal.getRole() == Role.ERG_LEAD) {
            if (!principal.getId().equals(erg.getErgLeadId())) {
                throw new ForbiddenOperationException("You may only view memberships of your own ERG chapter");
            }
        }
        return membershipRepository.findByErg_Id(ergId, pageable).map(membershipMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipResponse getOwn(Long ergId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ERGMembership membership = membershipRepository.findByErg_IdAndEmployeeUserId(ergId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You are not a member of this ERG"));
        return membershipMapper.toResponse(membership);
    }

    @Override
    @Transactional
    public MembershipResponse update(Long ergId, Long membershipId, UpdateMembershipRequest request) {
        ergService.requireManageableChapter(ergId);
        ERGMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("ERGMembership", membershipId));
        if (!membership.getErg().getId().equals(ergId)) {
            throw new ForbiddenOperationException("Membership does not belong to ERG " + ergId);
        }
        membership.setRole(request.role());
        membership.setStatus(request.status());
        membership = membershipRepository.save(membership);
        ergService.recomputeMemberCount(ergId);
        auditLogWriter.record("UPDATE_MEMBERSHIP", "ERGMembership", membership.getId());
        return membershipMapper.toResponse(membership);
    }
}
