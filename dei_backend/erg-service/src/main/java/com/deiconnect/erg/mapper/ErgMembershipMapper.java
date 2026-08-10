package com.deiconnect.erg.mapper;

import com.deiconnect.erg.dto.MembershipResponse;
import com.deiconnect.erg.entity.ERGMembership;
import org.springframework.stereotype.Component;

@Component
public class ErgMembershipMapper {
    public MembershipResponse toResponse(ERGMembership membership) {
        return new MembershipResponse(
                membership.getId(),
                membership.getErg() == null ? null : membership.getErg().getId(),
                membership.getEmployeeUserId(),
                membership.getEmployeeId(),
                membership.getRole(),
                membership.getJoinDate(),
                membership.getStatus(),
                membership.getCreatedDate());
    }
}
