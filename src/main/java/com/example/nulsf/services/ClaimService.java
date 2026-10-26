package com.example.nulsf.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nulsf.model.Claim;
import com.example.nulsf.model.Item;
import com.example.nulsf.model.User;
import com.example.nulsf.repository.ClaimRepository;
import com.example.nulsf.repository.ItemRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    @Transactional
    public Claim fileClaim(Integer itemId, User claimer, String proofDescription) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        // Check if already claimed by this user
        if (claimRepository.existsByItemAndClaimerAndClaimStatusNot(item, claimer, "REJECTED")) {
            throw new IllegalStateException("You already have an active claim filed for this item!");
        }

        Claim claim = new Claim(item, claimer, proofDescription);
        claim = claimRepository.save(claim);

        auditService.log("CLAIM_SUBMITTED", claimer.getEmail(), "ITEM", String.valueOf(itemId), 
                "User filed ownership claim with proof details");

        return claim;
    }

    @Transactional
    public Claim approveClaim(Long claimId, User admin, String remarks) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));

        // Generate verification pickup code
        String code = "NU-C" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        claim.setClaimStatus("APPROVED");
        claim.setVerificationCode(code);
        claim.setAdminRemarks(remarks != null ? remarks : "Claim approved. Present verification pass at NU LSF Office.");
        claim.setReviewedAt(LocalDateTime.now());
        claimRepository.save(claim);

        // Update item status
        Item item = claim.getItem();
        item.setStatus("CLAIMED");
        item.setResolutionNotes("Claimed by " + claim.getClaimer().getFullName() + " (Pass: " + code + ")");
        itemRepository.save(item);

        // Notify claimant
        notificationService.notifyUser(
                claim.getClaimer(),
                "Claim Approved! Ready for Pickup",
                "Your claim for '" + item.getItemName() + "' was APPROVED! Verification Pass Code: " + code + ". Please visit the NU LSF Office.",
                "/claims/my-claims",
                "CLAIM_STATUS"
        );

        auditService.log("CLAIM_APPROVED", admin.getEmail(), "CLAIM", claimId.toString(), 
                "Approved claim for item " + item.getItemId() + ", issued pass " + code);

        return claim;
    }

    @Transactional
    public Claim rejectClaim(Long claimId, User admin, String remarks) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));

        claim.setClaimStatus("REJECTED");
        claim.setAdminRemarks(remarks != null ? remarks : "Insufficient ownership proof provided.");
        claim.setReviewedAt(LocalDateTime.now());
        claimRepository.save(claim);

        // Notify claimant
        notificationService.notifyUser(
                claim.getClaimer(),
                "Claim Status Update: Needs More Info / Rejected",
                "Your claim for '" + claim.getItem().getItemName() + "' was not approved. Remarks: " + claim.getAdminRemarks(),
                "/claims/my-claims",
                "CLAIM_STATUS"
        );

        auditService.log("CLAIM_REJECTED", admin.getEmail(), "CLAIM", claimId.toString(), 
                "Rejected claim. Reason: " + remarks);

        return claim;
    }

    public List<Claim> getUserClaims(User user) {
        return claimRepository.findByClaimerOrderByClaimDateDesc(user);
    }

    public List<Claim> getPendingClaims() {
        return claimRepository.findByClaimStatusIgnoreCaseOrderByClaimDateDesc("PENDING");
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public Optional<Claim> getClaim(Long claimId) {
        return claimRepository.findById(claimId);
    }

    public long countPendingClaims() {
        return claimRepository.countByClaimStatusIgnoreCase("PENDING");
    }
}
