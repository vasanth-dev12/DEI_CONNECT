package com.deiconnect.erg.service;

import com.deiconnect.erg.dto.MembershipResponse;
import com.deiconnect.erg.dto.UpdateMembershipRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ErgMembershipService {

    MembershipResponse join(Long ergId);

    void leave(Long ergId);

    Page<MembershipResponse> list(Long ergId, Pageable pageable);

    MembershipResponse getOwn(Long ergId);

    MembershipResponse update(Long ergId, Long membershipId, UpdateMembershipRequest request);
}
