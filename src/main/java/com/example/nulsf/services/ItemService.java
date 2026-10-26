package com.example.nulsf.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nulsf.model.Item;
import com.example.nulsf.model.User;
import com.example.nulsf.repository.ItemRepository;
import com.example.nulsf.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    public List<Item> searchItems(String keyword, String category, String status) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanCategory = (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) ? category.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) ? status.trim() : null;

        if (cleanKeyword == null && cleanCategory == null && cleanStatus == null) {
            return itemRepository.findAll().stream()
                    .sorted((a, b) -> Integer.compare(b.getItemId(), a.getItemId()))
                    .collect(Collectors.toList());
        }

        return itemRepository.searchItems(cleanKeyword, cleanCategory, cleanStatus);
    }

    public List<Item> getRecentLostItems() {
        return itemRepository.findByStatusIgnoreCase("PENDING_CLAIM").stream()
                .sorted((a, b) -> Integer.compare(b.getItemId(), a.getItemId()))
                .limit(6)
                .collect(Collectors.toList());
    }

    public List<Item> getRecentFoundItems() {
        return itemRepository.findAll().stream()
                .filter(i -> "AVAILABLE".equalsIgnoreCase(i.getStatus()) || "PENDING_SURRENDER".equalsIgnoreCase(i.getStatus()))
                .sorted((a, b) -> Integer.compare(b.getItemId(), a.getItemId()))
                .limit(6)
                .collect(Collectors.toList());
    }

    public List<Item> getItemsAwaitingIntake() {
        return itemRepository.findByStatusInIgnoreCase(Arrays.asList("PENDING_SURRENDER", "PENDING_CLAIM"));
    }

    @Transactional
    public Item receiveFoundItem(Integer itemId, User admin, String storageLocation, String notes) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        item.setStatus("AVAILABLE");
        item.setSurrenderDate(LocalDate.now());
        item.setStorageLocation(storageLocation != null && !storageLocation.trim().isEmpty() ? storageLocation.trim() : "Main Office Locker");
        if (notes != null && !notes.trim().isEmpty()) {
            item.setResolutionNotes(notes.trim());
        }
        itemRepository.save(item);

        auditService.log("ITEM_INTAKE_RECEIVED", admin.getEmail(), "ITEM", String.valueOf(itemId),
                "Item received at office, placed in " + item.getStorageLocation());

        // Check if original reporter can be notified
        if (item.getReportedBy() != null) {
            userRepository.findById(item.getReportedBy().longValue()).ifPresent(reporter -> {
                notificationService.notifyUser(
                        reporter,
                        "Found Item Surrender Confirmed",
                        "The item '" + item.getItemName() + "' was verified and safely stored in " + item.getStorageLocation() + ".",
                        "/catalog",
                        "INTAKE"
                );
            });
        }

        return item;
    }

    @Transactional
    public void updateStatus(Integer itemId, String newStatus, User admin, String notes) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        item.setStatus(newStatus);
        if (notes != null) {
            item.setResolutionNotes(notes);
        }
        itemRepository.save(item);

        auditService.log("ITEM_STATUS_UPDATED", admin.getEmail(), "ITEM", String.valueOf(itemId),
                "Status set to " + newStatus);
    }

    /**
     * Smart Matching Algorithm (Feature 7)
     * Compares a given item against opposite items to detect potential matches!
     */
    public List<ItemMatchResult> findPotentialMatches(Item target) {
        if (target == null) return Collections.emptyList();

        boolean isTargetLost = target.isLost();
        List<Item> candidates = itemRepository.findAll().stream()
                .filter(c -> !c.getItemId().equals(target.getItemId()))
                .filter(c -> isTargetLost ? c.isFound() : c.isLost())
                .filter(c -> !"CLAIMED".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());

        List<ItemMatchResult> matches = new ArrayList<>();

        for (Item candidate : candidates) {
            int score = 0;
            List<String> matchReasons = new ArrayList<>();

            // 1. Category match (high weight)
            if (target.getCategory() != null && target.getCategory().equalsIgnoreCase(candidate.getCategory())) {
                score += 40;
                matchReasons.add("Same Category: " + target.getCategory());
            }

            // 2. Color match
            if (target.getColor() != null && candidate.getColor() != null &&
                    !target.getColor().trim().isEmpty() &&
                    candidate.getColor().toLowerCase().contains(target.getColor().toLowerCase().trim())) {
                score += 25;
                matchReasons.add("Matching Color: " + target.getColor());
            }

            // 3. Location match
            if (target.getLocation() != null && candidate.getLocation() != null &&
                    !target.getLocation().trim().isEmpty() &&
                    (candidate.getLocation().toLowerCase().contains(target.getLocation().toLowerCase()) ||
                     target.getLocation().toLowerCase().contains(candidate.getLocation().toLowerCase()))) {
                score += 20;
                matchReasons.add("Similar Campus Location: " + target.getLocation());
            }

            // 4. Name keyword match
            if (target.getItemName() != null && candidate.getItemName() != null) {
                String[] words = target.getItemName().toLowerCase().split("\\s+");
                for (String w : words) {
                    if (w.length() > 2 && candidate.getItemName().toLowerCase().contains(w)) {
                        score += 15;
                        matchReasons.add("Keyword match: '" + w + "'");
                        break;
                    }
                }
            }

            if (score >= 35) {
                matches.add(new ItemMatchResult(candidate, score, matchReasons));
            }
        }

        matches.sort((a, b) -> Integer.compare(b.score, a.score));
        return matches;
    }

    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long totalItems = itemRepository.count();
        long pendingLost = itemRepository.countByStatusIgnoreCase("PENDING_CLAIM");
        long pendingSurrender = itemRepository.countByStatusIgnoreCase("PENDING_SURRENDER");
        long available = itemRepository.countByStatusIgnoreCase("AVAILABLE");
        long claimed = itemRepository.countByStatusIgnoreCase("CLAIMED");

        stats.put("totalItems", totalItems);
        stats.put("pendingLost", pendingLost);
        stats.put("pendingSurrender", pendingSurrender);
        stats.put("available", available);
        stats.put("claimed", claimed);
        return stats;
    }

    public static class ItemMatchResult {
        public final Item item;
        public final int score;
        public final List<String> reasons;

        public ItemMatchResult(Item item, int score, List<String> reasons) {
            this.item = item;
            this.score = score;
            this.reasons = reasons;
        }

        public Item getItem() { return item; }
        public int getScore() { return score; }
        public List<String> getReasons() { return reasons; }
    }
}
