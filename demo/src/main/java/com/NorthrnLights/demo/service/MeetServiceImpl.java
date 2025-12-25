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
            throw new IllegalArgumentException("Start date/time cannot be null");
        }
        if (meetRequest.getDateTimeEnd() == null) {
            throw new IllegalArgumentException("End date/time cannot be null");
        }
        if (meetRequest.getDateTimeEnd().isBefore(meetRequest.getDateTimeStart())) {
            throw new IllegalArgumentException("End date/time cannot be before start date/time");
        }

        Meet meet = new Meet();
        meet.setTitle(meetRequest.getTitle());
        meet.setDescription(meetRequest.getDescription());
        meet.setLinkOfMeet(meetRequest.getLinkOfMeet());
        
        // Se linkRecordClass não foi fornecido, usar o mesmo link
        if (meetRequest.getLinkRecordClass() != null && !meetRequest.getLinkRecordClass().trim().isEmpty()) {
            meet.setLinkRecordClass(meetRequest.getLinkRecordClass());
        } else {
            meet.setLinkRecordClass(meetRequest.getLinkOfMeet());
        }
        
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

        List<Meet> meets;
        
        if (id != null) {
            Optional<Meet> meet = meetRepository.findById(id);
            meets = meet.map(List::of).orElse(List.of());
        } else if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
            meets = meetRepository.findByDateTimeStartBetween(startDate, endDate);
        } else if (startDate != null) {
            meets = meetRepository.findBydateTimeEndAfter(startDate);
        } else if (endDate != null) {
            meets = meetRepository.findBydateTimeStartBefore(endDate);
        } else {
            // Se nenhum filtro for passado, retorna tudo
            meets = meetRepository.findAll();
        }
        
        // Finalizar automaticamente meets expirados
        LocalDateTime now = LocalDateTime.now();
        for (Meet meet : meets) {
            if (meet.getDateTimeEnd() != null && meet.getDateTimeEnd().isBefore(now)) {
                log.debug("Meet {} expirado em {}", meet.getId(), meet.getDateTimeEnd());
            }
        }
        
        return meets;
    }


    public List<Meet> getAllMeets() {
        log.info("searching all meets");
        List<Meet> meets = meetRepository.findAll();
        
        // Finalizar automaticamente meets expirados
        LocalDateTime now = LocalDateTime.now();
        for (Meet meet : meets) {
            if (meet.getDateTimeEnd() != null && meet.getDateTimeEnd().isBefore(now)) {
                // Meet já expirou - pode adicionar lógica adicional aqui se necessário
                log.debug("Meet {} expirado em {}", meet.getId(), meet.getDateTimeEnd());
            }
        }
        
        return meets;
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
            if (meetDetails.getTitle() != null) {
                meet.setTitle(meetDetails.getTitle());
            }
            if (meetDetails.getDescription() != null) {
                meet.setDescription(meetDetails.getDescription());
            }
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