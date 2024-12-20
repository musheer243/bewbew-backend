package com.blog_app.ServiceImpl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.blog_app.exceptions.PostUpdateDataNotFoundException;
import com.blog_app.services.FIleServiceMedia;
import com.blog_app.services.VirusScanService;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
@Service
public class FIleServiceMediaImpl implements FIleServiceMedia {
	
		@Value("${project.image}")
		private String path;
		
		 @Value("${aws.s3.bucket:default-bucket}")
			private String s3BucketName;
			
		    @Value("${aws.s3.region:default-region}")
			private String s3Region;
			
		    @Value("${aws.access.key:default-access-key}")
			private String accessKey;
			
		    @Value("${aws.secret.key:default-secret-key}")
			private String secretKey;
		
		private S3Client s3Client;
		
		@Autowired
		private Environment environment;
		
		@Autowired
		private VirusScanService virusScanService;
		
		
		 	@PostConstruct
		    public void init() {
		        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
		        s3Client = S3Client.builder()
		                .region(Region.of(s3Region))
		                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
		                .build();
		    }
		
		 	@Override
		 	public List<String> uploadMedia(List<MultipartFile> files) throws IOException {
		 	    List<String> uploadedFileNames = new ArrayList<>();
		 	   List<String> allowedTypes = Arrays.asList(
		 			    // Image formats
		 			    "image/jpeg",   // JPEG format
		 			    "image/jpg",    // JPG format
		 			    "image/png",    // PNG format
		 			    "image/gif",    // GIF format
		 			    "image/bmp",    // BMP (Bitmap) format
		 			    "image/tiff",   // TIFF format
		 			    "image/webp",   // WEBP format
		 			    "image/svg+xml", // SVG format
		 			    "image/jfif",
		 			   "application/octet-stream",

		 			    // Video formats
		 			    "video/mp4",    // MP4 format
		 			    "video/avi",    // AVI format
		 			    "video/mpeg",   // MPEG format
		 			    "video/quicktime", // MOV (QuickTime) format
		 			    "video/x-ms-wmv", // WMV format
		 			    "video/x-flv",  // FLV format
		 			    "video/webm"    // WEBM format
		 			);


			    for (MultipartFile file : files) {
			        String fileType = file.getContentType();
			        
			     // Scan the file before uploading
//			        if (!virusScanService.scanFile(file)) {
//			            throw new PostUpdateDataNotFoundException("File failed virus scan.");
//			        }
			        
			        // Validate file type
			        if (!allowedTypes.contains(fileType)) {
			            throw new PostUpdateDataNotFoundException("Invalid file type: " + fileType);
			        }

			        // Validate file size (max 100MB for multimedia)
			        if (file.getSize() > 100 * 1024 * 1024) {
			            throw new IllegalArgumentException("File size exceeds 100MB limit");
			        }

			        String originalFileName = file.getOriginalFilename();
			        if (originalFileName == null || originalFileName.isBlank()) {
			            throw new PostUpdateDataNotFoundException("File name is invalid");
			        }

			        // Create a unique file name to avoid conflicts
			        String uniqueFileName = UUID.randomUUID().toString() + "_" + sanitizeFileName(originalFileName);

			        if (isDevelopmentProfile()) {
			            // Local storage for development
			            File directory = new File(path);
			            if (!directory.exists()) {
			                directory.mkdirs();  // Create directories if missing
			            }
			            String filePath = path + File.separator + uniqueFileName;
			            Files.copy(file.getInputStream(), Paths.get(filePath));
			            uploadedFileNames.add(uniqueFileName);  // Store local file name
			        } else if (isProductionProfile()) {
			            // Upload to S3 for production
			            File tempFile = new File(System.getProperty("java.io.tmpdir"), uniqueFileName);
			            file.transferTo(tempFile);  // Save file temporarily before uploading to S3
			            uploadFileToS3(uniqueFileName, tempFile);  // Upload to S3
			            uploadedFileNames.add("https://" + s3BucketName + ".s3." + s3Region + ".amazonaws.com/" + uniqueFileName);  // Store S3 URL
			        }
			    }

			    return uploadedFileNames;
			}

			private String sanitizeFileName(String fileName) {
			    return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
			}


	@Override
	public List<InputStream> getResource(List<String> fileNames) throws FileNotFoundException {
		
		List<InputStream> resources = new ArrayList<>();
		
		for(String fileName: fileNames) {
			String fullpath = path + File.separator + fileName;
			InputStream is = new FileInputStream(fullpath);
			resources.add(is);
		}
		
	  return resources;
	}
	
	 private void uploadFileToS3(String fileName, File file) {
	        try {
	            s3Client.putObject(
	                PutObjectRequest.builder()
	                    .bucket(s3BucketName)
	                    .key(fileName)
	                    .build(),
	                software.amazon.awssdk.core.sync.RequestBody.fromFile(file)
	            );
	        } catch (S3Exception e) {
	            e.printStackTrace();
	        }
	    }


	 private boolean isDevelopmentProfile() {
		    boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");
		    System.out.println("Is Development Profile Active: " + isDev);
		    return isDev;
		}

		private boolean isProductionProfile() {
		    boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("pro");
		    System.out.println("Is Production Profile Active: " + isProd);
		    return isProd;
		}
		
		@Override
		public void deleteFileFromS3(List<String> filenames) {
			for (String filename : filenames) {
		        try {
		            s3Client.deleteObject(b -> b.bucket(s3BucketName).key(filename));
		            System.out.println("File deleted successfully from S3: " + filename);
		        } catch (S3Exception e) {
		            e.printStackTrace();
		            throw new PostUpdateDataNotFoundException("Failed to delete the file from S3: " + filename);
		        }
		    }
		}
		
		@Override
		public void deleteLocalFiles(List<String> fileNames) {
			for (String fileName : fileNames) {
		        try {
		            // Create the full path to the local file
		            String fullPath = path + File.separator + fileName;
		            File file = new File(fullPath);
		            if (file.exists()) {
		                boolean deleted = file.delete();
		                if (deleted) {
		                    System.out.println("Local file deleted successfully: " + fullPath);
		                } else {
		                    System.out.println("Failed to delete local file: " + fullPath);
		                }
		            } else {
		                System.out.println("Local file does not exist: " + fullPath);
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		            throw new RuntimeException("Error while deleting local file: " + fileName);
		        }
		    }
		}
		
			@Override
			public void deletePostFiles(List<String> fileNames) {
				if (isDevelopmentProfile()) {
			        // If in development, delete files locally
			        deleteLocalFiles(fileNames);
			    } else if (isProductionProfile()) {
			        // If in production, delete files from S3
			        List<String> s3FileNames = new ArrayList<>();
			        
			        // Extract only the file name from the S3 URLs
			        for (String fileName : fileNames) {
			            if (fileName.startsWith("https://")) {
			                fileName = fileName.substring(fileName.lastIndexOf("/") + 1); // Extract the file name
			            }
			            s3FileNames.add(fileName);
			        }
			        deleteFileFromS3(s3FileNames);
			    }
			}
		
			
}
