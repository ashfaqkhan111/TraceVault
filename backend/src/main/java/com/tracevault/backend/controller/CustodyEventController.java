package com.tracevault.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.CustodyEvent;
import com.tracevault.backend.service.CustodyEventService;
import com.tracevault.backend.service.CustodyEventService.CustodyEventInput;

@RestController
@RequestMapping("/api/evidence/{evidenceId}/custody-events")
public class CustodyEventController {
    private final CustodyEventService custodyEventService;

    public CustodyEventController(CustodyEventService custodyEventService) {
        this.custodyEventService = custodyEventService;
    }

    @PostMapping
    public CustodyEvent append(@PathVariable UUID evidenceId, @RequestBody CustodyEventInput input,
                               Authentication authentication) {
        String authenticatedEmail = authentication == null ? null : authentication.getName();
        return custodyEventService.appendUserEvent(evidenceId, authenticatedEmail, input);
    }

    @GetMapping
    public List<CustodyEvent> getTimeline(@PathVariable UUID evidenceId) {
        return custodyEventService.getTimeline(evidenceId);
    }
}
