package com.NorthrnLights.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


import java.time.LocalDateTime;

@Entity
@Data
public class Meet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String linkOfMeet;
    private LocalDateTime dateTimeStart;
    private LocalDateTime dateTimeEnd;
    private long presentInClass;
    private String linkRecordClass;
}
