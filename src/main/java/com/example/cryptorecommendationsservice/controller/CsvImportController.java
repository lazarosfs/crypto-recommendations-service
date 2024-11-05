package com.example.cryptorecommendationsservice.controller;

import com.example.cryptorecommendationsservice.service.CsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/import")
public class CsvImportController {

    private final CsvImportService csvImportService;
    private final ResourceLoader resourceLoader;

    @Autowired
    public CsvImportController(CsvImportService csvImportService, ResourceLoader resourceLoader) {
        this.csvImportService = csvImportService;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Loads all CSV files from the resources/csv directory after application startup.
     */
    @PostConstruct
    public void loadCsvFilesOnStartup() {
        try {
            // Load all CSV files in the resources/csv directory
            // Use PathMatchingResourcePatternResolver to load resources with a pattern
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:csv/*.csv");

            // Iterate through each CSV file resource
            for (Resource resource : resources) {
                try (InputStream inputStream = resource.getInputStream()) {
                    // Directly call the import service with InputStream
                    csvImportService.importCryptoData(inputStream);
                    System.out.println("Loaded CSV file: " + resource.getFilename());
                } catch (Exception e) {
                    System.err.println("Failed to load CSV file: " + resource.getFilename() + " due to " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading CSV files: " + e.getMessage());
        }
    }

    /**
     * Endpoint to import cryptocurrency data from a CSV file.
     *
     * @param file The CSV file containing crypto data.
     * @return A response message indicating success or failure of the import process.
     */
    @Operation(summary = "Import crypto data from CSV file", description = "Processes and imports cryptocurrency data from an uploaded CSV file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV data imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data in CSV file", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Error processing CSV file", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/crypto")
    public ResponseEntity<String> importCryptoData(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "CSV file containing crypto data", required = true,
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            csvImportService.importCryptoData(inputStream);
            return ResponseEntity.ok("CSV data imported successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing CSV file.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data in CSV file.");
        }
    }
}
