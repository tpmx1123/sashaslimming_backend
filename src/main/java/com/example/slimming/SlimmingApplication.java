package com.example.slimming;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class SlimmingApplication {
	public static void main(String[] args) {
		// Determine the correct path for .env file
		// Try multiple locations: current directory, backend directory, or project root
		String[] possiblePaths = {
			".env",                                    // Current directory
			"backend/.env",                            // Backend subdirectory
			"../backend/.env",                        // Parent/backend
			System.getProperty("user.dir") + File.separator + ".env",  // User dir
			System.getProperty("user.dir") + File.separator + "backend" + File.separator + ".env"  // User dir/backend
		};
		
		Dotenv dotenv = null;
		for (String path : possiblePaths) {
			try {
				File envFile = new File(path);
				if (envFile.exists() && envFile.isFile()) {
					// Found the .env file, load it
					if (path.contains(File.separator)) {
						// Extract directory and filename
						int lastSep = path.lastIndexOf(File.separator);
						String dir = path.substring(0, lastSep);
						String filename = path.substring(lastSep + 1);
						dotenv = Dotenv.configure()
								.directory(dir)
								.filename(filename)
								.load();
					} else {
						dotenv = Dotenv.configure()
								.directory("./")
								.filename(path)
								.load();
					}
					System.out.println("Loaded .env file from: " + envFile.getAbsolutePath());
					break;
				}
			} catch (Exception e) {
				// Continue to next path
			}
		}
		
		// If still not found, try default location (current directory)
		if (dotenv == null) {
			try {
				dotenv = Dotenv.configure()
						.ignoreIfMissing()
						.load();
			} catch (Exception e) {
				System.err.println("Warning: Could not load .env file. Using system environment variables only.");
			}
		}
		
		// Set system properties from .env file (Spring Boot reads these via ${VAR} syntax)
		if (dotenv != null) {
			dotenv.entries().forEach(entry -> {
				String key = entry.getKey();
				String value = entry.getValue();
				// Only set if not already set as system property or environment variable
				// System properties take precedence, then environment variables, then .env file
				if (System.getProperty(key) == null && System.getenv(key) == null) {
					System.setProperty(key, value);
					System.out.println("Loaded environment variable: " + key);
				}
			});
		} else {
			System.err.println("ERROR: .env file not found! Please create .env file in backend directory.");
			System.err.println("Copy .env.example to .env and fill in your values.");
		}
		
		SpringApplication.run(SlimmingApplication.class, args);
	}

}

