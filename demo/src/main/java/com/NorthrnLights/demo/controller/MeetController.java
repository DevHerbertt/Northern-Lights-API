package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.Meet;
import com.NorthrnLights.demo.dto.MeetDTO;
import com.NorthrnLights.demo.service.MeetServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
// MeetController.java - ATUALIZADO
@RestController
@RequestMapping("/meets")
@RequiredArgsConstructor
public class MeetController {

    private final MeetServiceImpl meetService;

    @PostMapping
    public ResponseEntity<Meet> create(@RequestBody MeetDTO dto) {
        return ResponseEntity.ok(meetService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<Meet>> findWithFilters(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        return ResponseEntity.ok(meetService.findWithFilters(id, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meet> getById(@PathVariable Long id) {
        return ResponseEntity.of(meetService.getAllMeets()
                .stream()
                .filter(m -> m.getId().equals(id))
                .findFirst());
    }

    @PutMapping("/{id}/presentCount")
    public ResponseEntity<Meet> updatePresentCount(@PathVariable Long id, @RequestParam int newCount) {
        return ResponseEntity.ok(meetService.updatePresentCount(id, newCount));
    }

    @GetMapping("/quantity")
    public ResponseEntity<Integer> getQuantityMeets() {
        return ResponseEntity.ok(meetService.getQuantityMeets());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meet> updateMeet(@PathVariable Long id, @RequestBody MeetDTO dto) {
        return ResponseEntity.ok(meetService.updateMeet(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meetService.deleteMeet(id);
        return ResponseEntity.noContent().build();
    }
}