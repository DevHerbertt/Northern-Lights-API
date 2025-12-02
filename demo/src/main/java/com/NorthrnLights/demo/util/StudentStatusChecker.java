package com.NorthrnLights.demo.util;

import com.NorthrnLights.demo.domain.Status;
import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentStatusChecker {

    private final StudentRepository studentRepository;

    /**
     * Deactivate students who have not logged in for more than 15 days
     */
    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    @Transactional
    public void deactivateInactiveStudents() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(15);
        List<Student> inactiveStudents = studentRepository.findByStatusAndLastLoginBefore(Status.ACTIVE, cutoffDate);

        if (inactiveStudents.isEmpty()) {
            log.info("✅ No inactive students found to deactivate.");
            return;
        }

        inactiveStudents.forEach(student -> {
            student.setStatus(Status.INACTIVE);
            studentRepository.save(student);
            log.info("🚫 Student deactivated: {} - Last login: {}", student.getEmail(), student.getLastLogin());
        });

        log.info("🔁 Total students deactivated: {}", inactiveStudents.size());
    }

    /**
     * Reactivate students who were inactive but have logged in recently
     */
    @Scheduled(cron = "0 0 6 * * ?") // Runs daily at 6 AM
    @Transactional
    public void reactivateRecentlyActiveStudents() {
        LocalDateTime recentLoginCutoff = LocalDateTime.now().minusDays(1);
        List<Student> reactivatingStudents = studentRepository.findByStatusAndLastLoginAfter(Status.INACTIVE, recentLoginCutoff);

        if (reactivatingStudents.isEmpty()) {
            log.info("✅ No students to reactivate.");
            return;
        }

        reactivatingStudents.forEach(student -> {
            student.setStatus(Status.ACTIVE);
            studentRepository.save(student);
            log.info("✅ Student reactivated: {} - Last login: {}", student.getEmail(), student.getLastLogin());
        });

        log.info("🔁 Total students reactivated: {}", reactivatingStudents.size());
    }
}
