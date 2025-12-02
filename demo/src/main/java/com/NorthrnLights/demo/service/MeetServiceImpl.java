package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.Meet;
import com.NorthrnLights.demo.dto.MeetDTO;
import com.NorthrnLights.demo.repository.MeetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetServiceImpl  {

    private final MeetRepository meetRepository;


    public Meet create(MeetDTO meetRequest) {
        log.info("Salving the new Meet {}", meetRequest.getLinkOfMeet());
        
        // Validações básicas
        if (meetRequest.getLinkOfMeet() == null || meetRequest.getLinkOfMeet().trim().isEmpty()) {
            throw new IllegalArgumentException("Link of meet can`t be void");
        }
        
        if (meetRequest.getDateTimeStart() == null) {
            throw new IllegalArgumentException("");
        }
        if (meetRequest.getDateTimeEnd() == null) {
            throw new IllegalArgumentException("date and hour no can be null");
        }
        if (meetRequest.getDateTimeEnd().isBefore(meetRequest.getDateTimeStart())) {
            throw new IllegalArgumentException("End date/time cannot be before start date/time");
        }

        Meet meet = new Meet();
        meet.setLinkOfMeet(meetRequest.getLinkOfMeet());
        meet.setDateTimeStart(meetRequest.getDateTimeStart());
        meet.setDateTimeEnd(meetRequest.getDateTimeEnd());


        // 🟢 Setando número de presentes, default para 0 se vier nulo
        meet.setPresentInClass(
                meetRequest.getPresentInClass() != null ? meetRequest.getPresentInClass() : 0
        );

        return meetRepository.save(meet);
    }

    public Meet updatePresentCount(Long id, int newCount) {
        Meet meet = meetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meet not found with ID: " + id));

        meet.setPresentInClass(newCount);

        return meetRepository.save(meet);
    }

    public List<Meet> findWithFilters(Long id, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Searching meets with filters - ID: {}, StartDate: {}, EndDate: {}", id, startDate, endDate);

        if (id != null) {
            Optional<Meet> meet = meetRepository.findById(id);
            return meet.map(List::of).orElse(List.of());
        }

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
            return meetRepository.findByDateTimeStartBetween(startDate, endDate);
        }

        if (startDate != null) {
            return meetRepository.findBydateTimeEndAfter(startDate);
        }

        if (endDate != null) {
            return meetRepository.findBydateTimeStartBefore(endDate);
        }

        // Se nenhum filtro for passado, retorna tudo
        return meetRepository.findAll();
    }


    public List<Meet> getAllMeets() {
        log.info("searching all meets");
        return meetRepository.findAll();
    }

    public int getQuantityMeets() {
        log.info("searching Quantity meets");
        return meetRepository.findAll().size();
    }


    public Meet updateMeet(Long id, MeetDTO meetDetails) {
        log.info("update meet with ID: {}", id);
        
        Optional<Meet> existingMeet = meetRepository.findById(id);
        
        if (existingMeet.isPresent()) {
            Meet meet = existingMeet.get();
            
            // Atualiza apenas os campos fornecidos
            if (meetDetails.getLinkOfMeet() != null) {
                meet.setLinkOfMeet(meetDetails.getLinkOfMeet());
            }
            if (meetDetails.getLinkRecordClass() != null) {
                meet.setLinkRecordClass(meetDetails.getLinkRecordClass());
            }
            
            if (meetDetails.getDateTimeStart() != null) {
                meet.setDateTimeStart(meetDetails.getDateTimeStart());
            }

            if (meetDetails.getDateTimeEnd() != null) {
                meet.setDateTimeEnd(meetDetails.getDateTimeEnd());
            }

            return meetRepository.save(meet);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Meet not found with ID: " + id);
        }
    }


    public void deleteMeet(Long id) {
        log.info("deleting meet with ID: {}", id);
        
        if (!meetRepository.existsById(id)) {
            throw new RuntimeException("Meet not found with ID: " + id);
        }
        
        meetRepository.deleteById(id);
    }


}