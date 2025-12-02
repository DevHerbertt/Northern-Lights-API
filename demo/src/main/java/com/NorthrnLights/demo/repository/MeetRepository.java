package com.NorthrnLights.demo.repository;

import com.NorthrnLights.demo.domain.Meet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetRepository extends JpaRepository<Meet, Long> {
    List<Meet> findBydateTimeEndAfter(LocalDateTime dateTime);

    List<Meet> findBydateTimeStartBefore(LocalDateTime dateTime);

    List<Meet> findByDateTimeStartBetween(LocalDateTime start, LocalDateTime end);
}