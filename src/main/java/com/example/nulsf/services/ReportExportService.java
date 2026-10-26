package com.example.nulsf.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.nulsf.model.Claim;
import com.example.nulsf.model.Item;
import com.example.nulsf.repository.ClaimRepository;
import com.example.nulsf.repository.ItemRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ReportExportService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ClaimRepository claimRepository;

    public byte[] generateItemsCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Item ID,Item Name,Category,Status,Color,Location,Date Reported,Reported By,Storage Location,Notes\n");

        List<Item> items = itemRepository.findAll();
        for (Item item : items) {
            sb.append(escapeCsv(String.valueOf(item.getItemId()))).append(",");
            sb.append(escapeCsv(item.getItemName())).append(",");
            sb.append(escapeCsv(item.getCategory())).append(",");
            sb.append(escapeCsv(item.getStatus())).append(",");
            sb.append(escapeCsv(item.getColor())).append(",");
            sb.append(escapeCsv(item.getLocation())).append(",");
            sb.append(escapeCsv(item.getDateReported() != null ? item.getDateReported().toString() : "")).append(",");
            sb.append(escapeCsv(item.getPostedBy())).append(",");
            sb.append(escapeCsv(item.getStorageLocation())).append(",");
            sb.append(escapeCsv(item.getResolutionNotes())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateClaimsCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Claim ID,Item ID,Item Name,Claimer Name,Claimer Email,Date Filed,Status,Pass Code,Remarks\n");

        List<Claim> claims = claimRepository.findAll();
        for (Claim c : claims) {
            sb.append(escapeCsv(String.valueOf(c.getClaimId()))).append(",");
            sb.append(escapeCsv(String.valueOf(c.getItem().getItemId()))).append(",");
            sb.append(escapeCsv(c.getItem().getItemName())).append(",");
            sb.append(escapeCsv(c.getClaimer().getFullName())).append(",");
            sb.append(escapeCsv(c.getClaimer().getEmail())).append(",");
            sb.append(escapeCsv(c.getClaimDate() != null ? c.getClaimDate().toString() : "")).append(",");
            sb.append(escapeCsv(c.getClaimStatus())).append(",");
            sb.append(escapeCsv(c.getVerificationCode())).append(",");
            sb.append(escapeCsv(c.getAdminRemarks())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        String clean = value.replace("\"", "\"\"");
        return "\"" + clean + "\"";
    }
}
