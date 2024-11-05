package com.example.cryptorecommendationsservice.controller;

import com.example.cryptorecommendationsservice.service.CsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/import")
public class CsvImportController {

    private final CsvImportService csvImportService;
    private static final Logger logger = LoggerFactory.getLogger(CsvImportController.class);

    @Autowired
    public CsvImportController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    /**
     * Loads all CSV files from the resources/csv directory after application startup.
     */
    @PostConstruct
    public void loadCsvFilesOnStartup() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:csv/*.csv");

        for (Resource resource : resources) {
            try (InputStream inputStream = resource.getInputStream()) {
                csvImportService.importCryptoData(inputStream);
                logger.info("Loaded CSV file: {}", resource.getFilename());
            }
        }
    }

    /**
     * Endpoint to import cryptocurrency data from a CSV file.
     *
     * @param file The CSV file containing crypto data.
     * @return A response message indicating success or failure of the import process.
     * @throws Exception if an unexpected error occurs.
     */
    @Operation(summary = "Import crypto data from CSV file", description = "Processes and imports cryptocurrency data from an uploaded CSV file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV data imported successfully"),
            @ApiResponse(responseCode = "400", description = "The uploaded file contains invalid data.", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "An internal server error occurred while processing the file.", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/crypto")
    public ResponseEntity<String> importCryptoData(
            @RequestBody(
                    description = "CSV file containing crypto data",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("file") MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            csvImportService.importCryptoData(inputStream);
            return ResponseEntity.ok("CSV data imported successfully.");
        }
    }
}
