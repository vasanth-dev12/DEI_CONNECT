package com.deiconnect.erg.controller;

import com.deiconnect.erg.dto.CreateErgRequest;
import com.deiconnect.erg.dto.ErgResponse;
import com.deiconnect.erg.dto.UpdateErgRequest;
import com.deiconnect.erg.enums.ErgFocus;
import com.deiconnect.erg.enums.ErgStatus;
import com.deiconnect.erg.service.ErgService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ergs")
@RequiredArgsConstructor
public class ErgController {

    private final ErgService ergService;

    @PostMapping
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<ErgResponse> create(@Valid @RequestBody CreateErgRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ergService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','ADMIN','ERG_LEAD')")
    public ResponseEntity<ErgResponse> update(@PathVariable Long id,
                                              @Valid @RequestBody UpdateErgRequest request) {
        return ResponseEntity.ok(ergService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ergService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','DEI_MANAGER','HR_BIZ_PARTNER','ERG_LEAD','EXECUTIVE','ADMIN')")
    public ResponseEntity<Page<ErgResponse>> search(@RequestParam(required = false) ErgFocus focus,
                                                    @RequestParam(required = false) ErgStatus status,
                                                    @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(ergService.search(focus, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','DEI_MANAGER','HR_BIZ_PARTNER','ERG_LEAD','EXECUTIVE','ADMIN')")
    public ResponseEntity<ErgResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ergService.getById(id));
    }

    @GetMapping("/internal/members/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getActiveMemberCount(@RequestParam(required = false) String scope,
                                                     @RequestParam(required = false) String scopeValue,
                                                     @RequestParam(required = false) Long hrId) {
        return ResponseEntity.ok(ergService.getActiveMemberCount(scope, scopeValue, hrId));
    }
}
