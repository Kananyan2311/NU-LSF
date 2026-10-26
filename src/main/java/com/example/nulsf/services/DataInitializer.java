package com.example.nulsf.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.nulsf.model.Item;
import com.example.nulsf.model.User;
import com.example.nulsf.repository.ItemRepository;
import com.example.nulsf.repository.UserRepository;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            // Seed Admin if not exists
            if (userRepository.findByEmail("admin@national-u.edu.ph").isEmpty()) {
                User admin = new User();
                admin.setFullName("NU LSF Administrator");
                admin.setEmail("admin@national-u.edu.ph");
                admin.setUsername("admin@national-u.edu.ph");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                admin.setContactNumber("0917-123-4567");
                admin.setStudentId("EMP-2024-001");
                admin.setDepartment("Student Affairs & Safety");
                userRepository.save(admin);
                System.out.println("Default Admin account seeded: admin@national-u.edu.ph / admin123");
            }

            // Seed Demo Student if not exists
            if (userRepository.findByEmail("student@students.nu-laguna.edu.ph").isEmpty()) {
                User student = new User();
                student.setFullName("Juan Dela Cruz");
                student.setEmail("student@students.nu-laguna.edu.ph");
                student.setUsername("student@students.nu-laguna.edu.ph");
                student.setPassword(passwordEncoder.encode("student123"));
                student.setRole("USER");
                student.setContactNumber("0928-765-4321");
                student.setStudentId("2024-102941");
                student.setDepartment("College of Computing & IT");
                userRepository.save(student);
                System.out.println("Default Student account seeded: student@students.nu-laguna.edu.ph / student123");
            }

            // Seed sample items if items table is empty
            if (itemRepository.count() == 0) {
                User admin = userRepository.findByEmail("admin@national-u.edu.ph").orElse(null);
                Integer adminId = admin != null ? admin.getUserId().intValue() : 1;

                Item i1 = new Item();
                i1.setItemName("Casio fx-991EX Scientific Calculator");
                i1.setCategory("Electronics");
                i1.setDescription("Black and white scientific calculator with minor scratch on the back protective lid.");
                i1.setColor("Black / White");
                i1.setLocation("Engineering Computer Lab 3");
                i1.setDateReported(LocalDate.now().minusDays(2));
                i1.setStatus("AVAILABLE");
                i1.setStorageLocation("Bin A-02, Shelf 1");
                i1.setPostedBy("Guard Ramirez");
                i1.setReportedBy(adminId);
                itemRepository.save(i1);

                Item i2 = new Item();
                i2.setItemName("NU Bulldog Navy Blue ID Lanyard with Card");
                i2.setCategory("IDs & Cards");
                i2.setDescription("Gold and navy lanyard with student card and beep card attached in transparent holder.");
                i2.setColor("Navy Blue / Gold");
                i2.setLocation("University Cafeteria 2nd Floor");
                i2.setDateReported(LocalDate.now().minusDays(1));
                i2.setStatus("AVAILABLE");
                i2.setStorageLocation("Bin B-01, Safe Drawer");
                i2.setPostedBy("Staff Morales");
                i2.setReportedBy(adminId);
                itemRepository.save(i2);

                Item i3 = new Item();
                i3.setItemName("Hydroflask 32oz Mustard Yellow Water Bottle");
                i3.setCategory("Personal Belongings");
                i3.setDescription("Bright yellow bottle with National University Bulldog sticker and boot protector.");
                i3.setColor("Yellow / Gold");
                i3.setLocation("Gymnasium Bleachers");
                i3.setDateReported(LocalDate.now().minusDays(3));
                i3.setStatus("PENDING_CLAIM"); // Lost report
                i3.setPostedBy("Juan Dela Cruz");
                i3.setReportedBy(adminId);
                itemRepository.save(i3);

                Item i4 = new Item();
                i4.setItemName("Lenovo 65W USB-C Laptop Charger");
                i4.setCategory("Electronics");
                i4.setDescription("Standard black Lenovo brick charger left plugged in near study carrel 14.");
                i4.setColor("Black");
                i4.setLocation("Main Library 3rd Floor");
                i4.setDateReported(LocalDate.now());
                i4.setStatus("PENDING_SURRENDER"); // Found, awaiting turn in
                i4.setPostedBy("Maria Santos");
                i4.setReportedBy(adminId);
                itemRepository.save(i4);

                System.out.println("Seeded initial Lost & Found items for NU LSF.");
            }
        } catch (Exception e) {
            System.err.println("Notice during data initialization: " + e.getMessage());
        }
    }
}
