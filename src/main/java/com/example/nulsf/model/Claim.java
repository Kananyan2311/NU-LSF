package com.example.nulsf.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_id")
    private Long claimId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "claimer_id", nullable = false)
    private User claimer;

    @Column(name = "claim_date")
    private LocalDate claimDate;

    @Column(name = "claim_status", length = 50)
    private String claimStatus; // "PENDING", "APPROVED", "REJECTED", "PICKED_UP"

    @Column(name = "proof_description", length = 2000)
    private String proofDescription;

    @Column(name = "admin_remarks", length = 1000)
    private String adminRemarks;

    @Column(name = "verification_code", length = 50)
    private String verificationCode; // Unique pickup pass code e.g. "NU-CLAIM-8921"

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public Claim() {}

    public Claim(Item item, User claimer, String proofDescription) {
        this.item = item;
        this.claimer = claimer;
        this.claimDate = LocalDate.now();
        this.claimStatus = "PENDING";
        this.proofDescription = proofDescription;
    }

    // Getters and Setters
    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public User getClaimer() { return claimer; }
    public void setClaimer(User claimer) { this.claimer = claimer; }

    public LocalDate getClaimDate() { return claimDate; }
    public void setClaimDate(LocalDate claimDate) { this.claimDate = claimDate; }

    public String getClaimStatus() { return claimStatus; }
    public void setClaimStatus(String claimStatus) { this.claimStatus = claimStatus; }

    public String getProofDescription() { return proofDescription; }
    public void setProofDescription(String proofDescription) { this.proofDescription = proofDescription; }

    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
