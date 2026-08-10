package com.deiconnect.notification.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.notification.dto.EmitNotificationRequest;
import com.deiconnect.notification.dto.NotificationResponse;
import com.deiconnect.notification.entity.Notification;
import com.deiconnect.notification.enums.NotificationCategory;
import com.deiconnect.notification.enums.NotificationStatus;
import com.deiconnect.notification.mapper.NotificationMapper;
import com.deiconnect.notification.repository.NotificationRepository;
import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements NotificationEmitter {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final AuditLogWriter auditLogWriter;

    @Override
    @Transactional
    public void emit(String employeeId, NotificationCategory category, String message) {
        persistAndVerify(employeeId, category, message);
    }

    @Transactional
    public NotificationResponse emit(EmitNotificationRequest request) {
        Notification notification = persistAndVerify(request.employeeId(), request.category(), request.message());
        auditLogWriter.record("EMIT_NOTIFICATION", "Notification", notification.getId());
        return notificationMapper.toResponse(notification);
    }

    private Notification persistAndVerify(String employeeId, NotificationCategory category, String message) {
        String normalisedEmployeeId = employeeId == null ? null : employeeId.trim();
        User recipient = userRepository.findByEmployeeId(normalisedEmployeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user found with employeeId: " + normalisedEmployeeId));

        Notification notification = notificationRepository.save(Notification.builder()
                .user(recipient)
                .employeeId(recipient.getEmployeeId())
                .category(category)
                .message(message)
                .status(NotificationStatus.UNREAD)
                .createdDate(Instant.now())
                .build());

        verifyDelivery(notification, recipient);
        return notification;
    }

    private void verifyDelivery(Notification notification, User recipient) {
        if (notification.getId() == null) {
            throw new NotificationDeliveryException(
                    "Notification creation failed for employeeId " + recipient.getEmployeeId()
                            + ": no identifier was generated");
        }

        boolean deliverable = notificationRepository.existsByIdAndEmployeeIdAndUser_IdAndStatus(
                notification.getId(), recipient.getEmployeeId(), recipient.getId(), NotificationStatus.UNREAD);
        if (!deliverable) {
            throw new NotificationDeliveryException(
                    "Notification " + notification.getId() + " could not be confirmed as delivered to employeeId "
                            + recipient.getEmployeeId());
        }

        boolean visibleInInbox = notificationRepository
                .findByUser_Id(recipient.getId(), PageRequest.of(0, 200))
                .getContent().stream()
                .anyMatch(n -> n.getId().equals(notification.getId()));

        long unread = notificationRepository.countByUser_IdAndStatus(
                recipient.getId(), NotificationStatus.UNREAD);

        log.info("Notification delivery verified: notificationId={}, employeeId={}, userId={}, "
                        + "category={}, createdInDb=true, employeeMatch=true, visibleInInbox={}, unreadCount={}",
                notification.getId(), recipient.getEmployeeId(), recipient.getId(),
                notification.getCategory(), visibleInInbox, unread);

        if (!visibleInInbox) {
            throw new NotificationDeliveryException(
                    "Notification " + notification.getId() + " is not visible in the notification list of employeeId "
                            + recipient.getEmployeeId());
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> listOwn(NotificationStatus status, Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Notification> page = (status == null)
                ? notificationRepository.findByUser_Id(userId, pageable)
                : notificationRepository.findByUser_IdAndStatus(userId, status, pageable);
        return page.map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getOwn(Long id) {
        return notificationMapper.toResponse(loadOwned(id));
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByUser_IdAndStatus(
                SecurityUtils.getCurrentUserId(), NotificationStatus.UNREAD);
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        Notification notification = loadOwned(id);
        notification.setStatus(NotificationStatus.READ);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponse dismiss(Long id) {
        Notification notification = loadOwned(id);
        notification.setStatus(NotificationStatus.DISMISSED);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllRead() {
        return notificationRepository.markAllRead(SecurityUtils.getCurrentUserId());
    }

    private Notification loadOwned(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (!notification.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ForbiddenOperationException("You may only access your own notifications");
        }
        return notification;
    }
}
