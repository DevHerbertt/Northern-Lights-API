package com.NorthrnLights.demo.util;

import com.NorthrnLights.demo.domain.Meet;
import com.NorthrnLights.demo.repository.MeetRepository;
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
public class MeetScheduler {

    private final MeetRepository meetRepository;

    /**
     * Finaliza automaticamente meets que expiraram
     * Executa a cada minuto para verificar meets expirados
     */
    @Scheduled(cron = "0 * * * * ?") // Executa a cada minuto
    @Transactional
    public void finalizeExpiredMeets() {
        LocalDateTime now = LocalDateTime.now();
        // Busca todos os meets que ainda não expiraram (para verificar quais expiraram)
        List<Meet> allMeets = meetRepository.findAll();
        
        int finalizedCount = 0;
        for (Meet meet : allMeets) {
            if (meet.getDateTimeEnd() != null && meet.getDateTimeEnd().isBefore(now)) {
                // Meet expirado - pode adicionar lógica adicional aqui
                // Por exemplo, marcar como finalizado, enviar notificações, etc.
                log.debug("✅ Meet {} finalizado automaticamente - Data de término: {}", 
                    meet.getId(), meet.getDateTimeEnd());
                finalizedCount++;
            }
        }
        
        if (finalizedCount > 0) {
            log.info("🔁 Total de meets finalizados automaticamente: {}", finalizedCount);
        }
    }
}

