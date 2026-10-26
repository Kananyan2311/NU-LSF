package com.example.nulsf.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.nulsf.model.Claim;
import com.example.nulsf.model.Item;
import com.example.nulsf.model.User;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByClaimerOrderByClaimDateDesc(User claimer);

    List<Claim> findByClaimStatusIgnoreCaseOrderByClaimDateDesc(String claimStatus);

    List<Claim> findByItemOrderByClaimDateDesc(Item item);

    Optional<Claim> findByVerificationCode(String verificationCode);

    long countByClaimStatusIgnoreCase(String claimStatus);

    boolean existsByItemAndClaimerAndClaimStatusNot(Item item, User claimer, String excludedStatus);
}
