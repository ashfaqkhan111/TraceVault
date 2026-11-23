package com.tracevault.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.InvestigationCase;
import com.tracevault.backend.enms.CaseStatus;
import com.tracevault.backend.enms.CasePriority;
import com.tracevault.backend.entity.User;

import com.tracevault.backend.repository.InvestigationCaseRepository;
import com.tracevault.backend.repository.UserRepository;

@Service 
public class InvestigationCaseService {

    private final InvestigationCaseRepository caseRepository;

    private final UserRepository userRepository;

    public InvestigationCaseService(InvestigationCaseRepository caseRepository, UserRepository userRepository){
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        
    }

    public InvestigationCase createCase(UUID userID, String caseNumber, String caseName,
                                        String description){
                                
            User user = userRepository.findById(userID).orElseThrow(() -> new RuntimeException("User not found."));

            if (caseRepository.existByCaseNumber(caseNumber)){
                throw new RuntimeException("Case number already exist.");
            }

            InvestigationCase investigationCase = new InvestigationCase();

            investigationCase.setCaseNumber(caseNumber);
            investigationCase.setTitle(caseName);
            investigationCase.setDescription(description);
            investigationCase.setStatus(CaseStatus.OPEN);

            return caseRepository.save(investigationCase);

                                        }
    public InvestigationCase getCaseById(UUID caseId){
        return caseRepository.findById(caseId).orElseThrow(() -> new RuntimeException("Investigation case not found."));

    }

    public List<InvestigationCase> getAllCases(){
        return caseRepository.findAll();
    }

    public List<InvestigationCase> getCaseByUser(UUID userId){
        return caseRepository.findByCreatedBy_UserId(userId);
    }

    public List<InvestigationCase> getCaseByStatus(CaseStatus caseStatus){
        return caseRepository.findByStatus(caseStatus);
    }

    @Transactional 
    public InvestigationCase updateCase(UUID caseId, String caseName, String description){
        InvestigationCase investigationCase = getCaseById(caseId);

        investigationCase.setTitle(caseName);
        investigationCase.setDescription(description);
        
        return caseRepository.save(investigationCase);
    }

    @Transactional 
    public InvestigationCase updateCaseStatus(UUID caseId, CaseStatus caseStatus){
        InvestigationCase investigationCase = getCaseById(caseId);
        investigationCase.setStatus(caseStatus);

        return caseRepository.save(investigationCase);
    }

    @Transactional 
    public InvestigationCase closeCase(UUID caseId){
        InvestigationCase investigationCase = getCaseById(caseId);

        investigationCase.setStatus(CaseStatus.CLOSED);

        return caseRepository.save(investigationCase);
    }

    public void deleteCase(UUID caseId){
        InvestigationCase investigationCase = getCaseById(caseId);

        caseRepository.delete(investigationCase);
    }

    
}
