package com.example.nulsf.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;

    private String itemName;

    private String category; // Electronics, Wallets, IDs & Cards, Keys, Books, Clothing, Others

    @Column(length = 2000)
    private String description;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    private String color;

    private String location; // Specific location on campus

    private LocalDate dateReported;

    private String status; // "PENDING_CLAIM" (Lost), "PENDING_SURRENDER" (Found, not handed in), "AVAILABLE" (At LSF Office), "CLAIMED"

    private String postedBy; 

    @Column(name = "reported_by")
    private Integer reportedBy; 

    private String storageLocation; // Storage locker/bin at NU LSF Office

    private LocalDate surrenderDate;

    private String turnedInTo;

    @Column(length = 1000)
    private String resolutionNotes;

    public Item() {}

    // Helper to determine original report category
    public boolean isLost() {
        return "PENDING_CLAIM".equalsIgnoreCase(status) || "LOST".equalsIgnoreCase(status);
    }

    public boolean isFound() {
        return "PENDING_SURRENDER".equalsIgnoreCase(status) || "AVAILABLE".equalsIgnoreCase(status) || "FOUND".equalsIgnoreCase(status);
    }

    public String getDisplayStatus() {
        if ("PENDING_CLAIM".equalsIgnoreCase(status) || "LOST".equalsIgnoreCase(status)) {
            return "LOST";
        } else if ("PENDING_SURRENDER".equalsIgnoreCase(status)) {
            return "FOUND (Awaiting Office Turn-in)";
        } else if ("AVAILABLE".equalsIgnoreCase(status)) {
            return "AT LSF OFFICE (Ready to Claim)";
        } else if ("CLAIMED".equalsIgnoreCase(status)) {
            return "CLAIMED & RETURNED";
        }
        return status != null ? status : "UNKNOWN";
    }

    // Getters and Setters
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getDateReported() { return dateReported; }
    public void setDateReported(LocalDate dateReported) { this.dateReported = dateReported; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }

    public Integer getReportedBy() { return reportedBy; }
    public void setReportedBy(Integer reportedBy) { this.reportedBy = reportedBy; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public LocalDate getSurrenderDate() { return surrenderDate; }
    public void setSurrenderDate(LocalDate surrenderDate) { this.surrenderDate = surrenderDate; }

    public String getTurnedInTo() { return turnedInTo; }
    public void setTurnedInTo(String turnedInTo) { this.turnedInTo = turnedInTo; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}
