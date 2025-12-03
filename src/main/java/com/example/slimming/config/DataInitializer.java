package com.example.slimming.config;

import com.example.slimming.entity.Admin;
import com.example.slimming.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin if not exists (fetched from database after creation)
        if (adminRepository.findByUsername("admin").isEmpty()) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@slimming.com");
            admin.setRole("ADMIN");
            adminRepository.save(admin);
            System.out.println("Default admin created: username=admin, password=admin123");
            System.out.println("Note: Password is stored encrypted in the database");
        }

        // Sample blogs removed - blogs should be created through admin panel
    }

}
