package com.investmentengine.portfolio.controller;


import com.investmentengine.portfolio.repository.DeadLetterRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dlq")
@RequiredArgsConstructor

public class DlqController {

    private final DeadLetterRepository deadLetterRepository;

    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPending(){
        return ResponseEntity.ok(deadLetterRepository.findAllPending());
    }


    @PostMapping("/{id}/reprocess")
    public ResponseEntity<Void> reprocess(@PathVariable Long id){
        deadLetterRepository.markReprocessed(id);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id){
        deadLetterRepository.markCancelled(id);
        return ResponseEntity.ok().build();

    }
}
