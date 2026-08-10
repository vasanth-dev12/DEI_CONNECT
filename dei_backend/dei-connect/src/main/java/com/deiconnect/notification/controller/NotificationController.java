package com.deiconnect.notification.controller;

import com.deiconnect.notification.dto.EmitNotificationRequest;
import com.deiconnect.notification.dto.NotificationResponse;
import com.deiconnect.notification.enums.NotificationStatus;
import com.deiconnect.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponse>> list(
            @RequestParam(required = false) NotificationStatus status,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(notificationService.listOwn(status, pageable));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> unreadCount() {
        return ResponseEntity.ok(notificationService.unreadCount());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getOwn(id));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(id));
    }

    @PutMapping("/{id}/dismiss")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> dismiss(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.dismiss(id));
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> markAllRead() {
        return ResponseEntity.ok(notificationService.markAllRead());
    }

    @PostMapping("/emit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> emit(@Valid @RequestBody EmitNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.emit(request));
    }

    @PostMapping("/internal/emit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> emitInternal(@Valid @RequestBody EmitNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.emit(request));
    }
}
