package com.NorthrnLights.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetEmailDTO {
    private String email;
    private String userName;
    private String meetTitle;
    private String meetDescription;
    private String meetLink;
    private LocalDateTime meetStartDate;
    private LocalDateTime meetEndDate;
}








