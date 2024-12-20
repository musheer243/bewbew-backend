package com.blog_app.ServiceImpl;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.blog_app.exceptions.PostUpdateDataNotFoundException;
import com.blog_app.services.VirusScanService;

import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

@Service
public class VirusScanServiceImpl implements VirusScanService {

    private static final Logger logger = LoggerFactory.getLogger(VirusScanServiceImpl.class);
    private final ClamavClient clamavClient;

    public VirusScanServiceImpl(@Value("${clamav.host}") String clamHost, @Value("${clamav.port}") int clamPort) {
        logger.info("Initializing ClamAV client with host: {} and port: {}", clamHost, clamPort);
        this.clamavClient = new ClamavClient(clamHost, clamPort);
        logger.info("ClamAV client initialized successfully");
    }

    @Override
    public boolean scanFile(MultipartFile file) throws IOException {
        logger.debug("Starting virus scan for file: {}", file.getOriginalFilename());

        // Scan the file using ClamAV
        ScanResult result;
        try {
            result = clamavClient.scan(file.getInputStream());
            logger.info("Virus scan completed for file: {}", file.getOriginalFilename());
        } catch (IOException e) {
            logger.error("Error occurred while scanning file: {}", file.getOriginalFilename(), e);
            throw e; // Rethrow the exception after logging
        }

        logger.debug("Scan result for file {}: {}", file.getOriginalFilename(), result);

        // Check the scan result
        if (result instanceof ScanResult.OK) {
            logger.info("No virus found in file: {}", file.getOriginalFilename());
            return true; // No virus found
        } else if (result instanceof ScanResult.VirusFound) {
            // Get details of the viruses found
            Map<String, Collection<String>> foundViruses = ((ScanResult.VirusFound) result).getFoundViruses();
            
            // Iterate over the map to get the virus names
            StringBuilder virusDetails = new StringBuilder("Virus(es) detected in file: " + file.getOriginalFilename() + " - ");
            foundViruses.forEach((virusName, details) -> {
                virusDetails.append(virusName).append(" ");
                logger.error("Virus detected: {} with details: {}", virusName, details);
            });

            throw new PostUpdateDataNotFoundException(virusDetails.toString());
        } else {
            logger.warn("Unexpected scan result for file: {}. Result: {}", file.getOriginalFilename(), result);
        }

        return false;
    }
}
