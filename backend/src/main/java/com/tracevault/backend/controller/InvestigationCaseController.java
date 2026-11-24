package com.tracevault.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.tracevault.backend.entity.InvestigationCase;
import com.tracevault.backend.enms.CaseStatus;
import com.tracevault.backend.enms.CasePriority;
import com.tracevault.backend.service.InvestigationCaseService;

@RestController 
@RequestMapping ("/api/user")
public class InvestigationCaseController {
    private final InvestigationCaseService caseService;

    public InvestigationCaseController(InvestigationCaseService caseService){
        this.caseService = caseService;
    }

    @PostMapping ("/user/{userId}")
    public InvestigationCase createCase(@PathVariable UUID userId, @RequestParam String caseNumber,
                                        @RequestParam String caseName, @RequestParam String description){
            
            return caseService.createCase(userId, caseNumber, caseName, description);
                                        }
                                
    @GetMapping ("/{caseId}")
    public InvestigationCase getCaseById(@PathVariable UUID caseId){
        return caseService.getCaseById(caseId);
    }

    public List<InvestigationCase> getAllCases(){
        return caseService.getAllCases();
    }

    @GetMapping ("/user/{userId}")
    public List<InvestigationCase> getCasesByUser(@PathVariable UUID userId){
        return caseService.getCaseByUser(userId);
    }

    @GetMapping ("/status/{status}")
    public List<InvestigationCase> getCaseByStatus(@PathVariable CaseStatus status){
        return caseService.getCaseByStatus(status);
    }

    @PutMapping ("/{caseId}")
    public InvestigationCase updateCase(@PathVariable UUID caseId, @RequestParam String caseName, @RequestParam String description){
        return caseService.updateCase(caseId, caseName, description);
    }

    @PutMapping ("/{caseId}/status")
    public InvestigationCase updateStatus(@PathVariable UUID caseId, @RequestParam CaseStatus caseStatus){
        return caseService.updateCaseStatus(caseId, caseStatus);
    }

    @PutMapping ("/{caseId}/close")
    public InvestigationCase closeCase(@PathVariable UUID caseId){
        return caseService.closeCase(caseId);
    }

    @DeleteMapping  ("/{caseId}")
    public String deleteCase(@PathVariable UUID caseId){
        caseService.deleteCase(caseId);

        return "Investigation case deleted successfully.";
    }
}
