package com.blog_app.ServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.blog_app.config.AppConstants;
import com.blog_app.services.TranslationService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

@Service
public class TranslationServiceImpl implements TranslationService {
	
	private final TranslateClient translateClient;

    public TranslationServiceImpl(
    		@Value("${aws.translate.access-key}") String accessKey,
            @Value("${aws.translate.secret-key}") String secretKey)  {
        // Initialize AWS Translate client with region and credentials
        this.translateClient = TranslateClient.builder()
            .region(Region.US_EAST_1) // Set the appropriate region, e.g., "us-east-1"
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ))
            .build();
    }

	@Override
	public String translateText(String text, String targetLanguage) {
		TranslateTextRequest request = TranslateTextRequest.builder()
	            .text(text)
	            .sourceLanguageCode("auto") // Detect the source language automatically
	            .targetLanguageCode(targetLanguage) // Target language code (e.g., "es" for Spanish)
	            .build();

	        TranslateTextResponse response = translateClient.translateText(request);
	        return response.translatedText();
	}


}
