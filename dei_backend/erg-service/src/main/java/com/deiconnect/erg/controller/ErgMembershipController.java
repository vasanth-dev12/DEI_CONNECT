package com.deiconnect.erg.controller;

import com.deiconnect.erg.dto.MembershipResponse;
import com.deiconnect.erg.dto.UpdateMembershipRequest;
import com.deiconnect.erg.service.ErgMembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ergs/{ergId}/memberships")
@RequiredArgsConstructor
public class ErgMembershipController {
    private final ErgMembershipService membershipService;
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<MembershipResponse> join(@PathVariable Long ergId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.join(ergId));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> leave(@PathVariable Long ergId) {
        membershipService.leave(ergId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<MembershipResponse> myMembership(@PathVariable Long ergId) {
        return ResponseEntity.ok(membershipService.getOwn(ergId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DEI_MANAGER','ERG_LEAD','EXECUTIVE','ADMIN')")
    public ResponseEntity<Page<MembershipResponse>> list(@PathVariable Long ergId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(membershipService.list(ergId, pageable));
    }

    @PutMapping("/{membershipId}")
    @PreAuthorize("hasRole('ERG_LEAD')")
    public ResponseEntity<MembershipResponse> update(@PathVariable Long ergId,
                                                     @PathVariable Long membershipId,
                                                     @Valid @RequestBody UpdateMembershipRequest request) {
        return ResponseEntity.ok(membershipService.update(ergId, membershipId, request));
    }
}
