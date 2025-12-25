package com.NorthrnLights.demo.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetDTO {
    private String title;
    private String description;
    private String linkOfMeet;
    private LocalDateTime dateTimeStart;
    private LocalDateTime dateTimeEnd;
    private Long presentInClass;
    private String linkRecordClass;
    
    // Setter customizado para aceitar String ISO com Z
    @JsonSetter("dateTimeStart")
    public void setDateTimeStartFromJson(Object dateTimeStartObj) {
        if (dateTimeStartObj == null) {
            this.dateTimeStart = null;
            return;
        }
        
        if (dateTimeStartObj instanceof LocalDateTime) {
            this.dateTimeStart = (LocalDateTime) dateTimeStartObj;
            return;
        }
        
        if (dateTimeStartObj instanceof String) {
            String dateTimeStartStr = (String) dateTimeStartObj;
            try {
                if (dateTimeStartStr.endsWith("Z")) {
                    this.dateTimeStart = LocalDateTime.ofInstant(
                        Instant.parse(dateTimeStartStr), 
                        ZoneId.systemDefault()
                    );
                } else {
                    this.dateTimeStart = LocalDateTime.parse(dateTimeStartStr);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid dateTimeStart format: " + dateTimeStartStr, e);
            }
        }
    }
    
    @JsonSetter("dateTimeEnd")
    public void setDateTimeEndFromJson(Object dateTimeEndObj) {
        if (dateTimeEndObj == null) {
            this.dateTimeEnd = null;
            return;
        }
        
        if (dateTimeEndObj instanceof LocalDateTime) {
            this.dateTimeEnd = (LocalDateTime) dateTimeEndObj;
            return;
        }
        
        if (dateTimeEndObj instanceof String) {
            String dateTimeEndStr = (String) dateTimeEndObj;
            try {
                if (dateTimeEndStr.endsWith("Z")) {
                    this.dateTimeEnd = LocalDateTime.ofInstant(
                        Instant.parse(dateTimeEndStr), 
                        ZoneId.systemDefault()
                    );
                } else {
                    this.dateTimeEnd = LocalDateTime.parse(dateTimeEndStr);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid dateTimeEnd format: " + dateTimeEndStr, e);
            }
        }
    }
}