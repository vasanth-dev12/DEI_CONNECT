package com.deiconnect.iam.bootstrap;

import com.deiconnect.common.enums.Role;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.enums.DepartmentName;
import com.deiconnect.iam.enums.UserStatus;
import com.deiconnect.iam.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private static final String ADMIN_NAME = "System Administrator";
    private static final String ADMIN_EMPLOYEE_ID = "ADM001";
    private static final String ADMIN_EMAIL = "adm001@deiconnect.com";
    private static final String ADMIN_PASSWORD = "Welcome@001";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL) || userRepository.existsByEmployeeId(ADMIN_EMPLOYEE_ID)) {
            log.info("Default admin '{}' already present — skipping seed.", ADMIN_EMAIL);
            return;
        }

        User admin = User.builder()
                .employeeId(ADMIN_EMPLOYEE_ID)
                .name(ADMIN_NAME)
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .departmentName(DepartmentName.INFRASTRUCTURE)
                .departmentId(DepartmentName.INFRASTRUCTURE.getId())
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);
        log.info("Seeded default admin user '{}' (employeeId={}).", ADMIN_EMAIL, ADMIN_EMPLOYEE_ID);
    }
}
