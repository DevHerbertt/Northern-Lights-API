package com.NorthrnLights.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetDTO {
    private String linkOfMeet;
    private LocalDateTime dateTimeStart;
    private LocalDateTime dateTimeEnd;
    private Long presentInClass;
    private String linkRecordClass;

}