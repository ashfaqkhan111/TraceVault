package com.tracevault.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.TimelineEvent;
import com.tracevault.backend.service.TimelineEventService;
import com.tracevault.backend.service.TimelineEventService.TimelineEventInput;

@RestController
@RequestMapping("/api/cases/{caseId}/timeline")
public class TimelineEventController {
    private final TimelineEventService timelineEventService;

    public TimelineEventController(TimelineEventService timelineEventService) {
        this.timelineEventService = timelineEventService;
    }

    @PostMapping
    public TimelineEvent append(@PathVariable UUID caseId, @RequestBody TimelineEventInput input) {
        return timelineEventService.append(caseId, input);
    }

    @GetMapping
    public List<TimelineEvent> getForCase(@PathVariable UUID caseId) {
        return timelineEventService.getForCase(caseId);
    }
}
