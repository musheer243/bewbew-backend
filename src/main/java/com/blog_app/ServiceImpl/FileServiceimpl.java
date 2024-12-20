package com.blog_app.ServiceImpl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import com.blog_app.services.FileService;
import com.blog_app.services.VirusScanService;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class FileServiceimpl implements FileService {

    @Value("${project.userimage}")
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
	private VirusScanService virusScanService;
	
	@Autowired
	private Environment environment;
	
	
	@PostConstruct
	public void init() {
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
		s3Client = S3Client.builder().region(Region.of(s3Region)).credentialsProvider(StaticCredentialsProvider.create(awsCreds)).build();
	}
	@Override
	public String uploadImage(MultipartFile file) throws IOException {
	    // Define allowed types for profile picture upload
	    List<String> allowedTypes = Arrays.asList(
	        "image/jpeg",   // JPEG format
	        "image/jpg",    // JPG format
	        "image/png",    // PNG format
	        "image/gif",    // GIF format
	        "image/bmp",    // BMP (Bitmap) format
	        "image/tiff",   // TIFF format
	        "image/webp",   // WEBP format
	        "image/svg+xml" // SVG format
//	        "image/jfif",    // JFIF format
//	        "application/octet-stream"  // Allow octet-stream for custom extension validation

	    );

	    // Scan the file before uploading
//	    if (!virusScanService.scanFile(file)) {
//	        throw new PostUpdateDataNotFoundException("image blocked becoz file not supported");
//	    }

	    // Validate file type
	    String fileType = file.getContentType();
	    if (!allowedTypes.contains(fileType)) {
	        throw new PostUpdateDataNotFoundException("Invalid file type: " + fileType);
	    }

	    // Validate file size (max 10MB for profile picture)
	    if (file.getSize() > 10 * 1024 * 1024) { // 10 MB limit
	        throw new IllegalArgumentException("File size exceeds 10MB limit");
	    }

	    // Get the original file name
	    String originalFileName = file.getOriginalFilename();
	    if (originalFileName == null || originalFileName.isBlank()) {
	        throw new PostUpdateDataNotFoundException("File name is invalid");
	    }
	    

	    // Create a unique file name to avoid conflicts
	    String uniqueFileName = UUID.randomUUID().toString() + "_" + sanitizeFileName(originalFileName);

	    if (isDevelopmentProfile()) {
	        // Local storage for development
	    	// Local storage for development
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdirs();  // Create directories if missing
            }
	        String filePath = path + File.separator + uniqueFileName;
	        Files.copy(file.getInputStream(), Paths.get(filePath));
	        return uniqueFileName;  // Return the local file name
	    } else if (isProductionProfile()) {
	        // Upload to S3 for production
	        File tempFile = new File(System.getProperty("java.io.tmpdir"), uniqueFileName);
	        file.transferTo(tempFile);  // Save file temporarily before uploading to S3
	        uploadFileToS3(uniqueFileName, tempFile);  // Upload to S3
	        return "https://" + s3BucketName + ".s3." + s3Region + ".amazonaws.com/" + uniqueFileName;  // Return S3 URL
	    }
	    return null;
	}
	
	private String sanitizeFileName(String fileName) {
	    return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
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


	


	@Override
	public InputStream getResource(String fileName) throws FileNotFoundException {
		
		String fullPath = path+File.separator+fileName;
		InputStream is = new FileInputStream(fullPath);
		
		//db logic to return inputStream
		return is;
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
	public void deleteFileFromS3(String filename) {
		try {
			s3Client.deleteObject(b -> b.bucket(s3BucketName).key(filename));
			System.out.println("file deleted sucessfully from S3 " + filename);
		} catch (S3Exception e) {
			e.printStackTrace();
			throw new PostUpdateDataNotFoundException("failed to delete the file from S3 " + filename);
		}
	}
	@Override
	public void deleteLocalFile(String fileName) {
		try {
	        // Create the path to the local file
	        String fullPath = path + File.separator + fileName; // Ensure 'path' is the local storage directory
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
	
	@Override
	public void deleteUserFile(String profilepic) {
	    if (isDevelopmentProfile()) {
	        deleteLocalFile(profilepic);
	    } else if (isProductionProfile()) {
	        if (profilepic.startsWith("https://")) {
	            profilepic = profilepic.substring(profilepic.lastIndexOf("/") + 1); // Extract just the file name
	        }
	        deleteFileFromS3(profilepic);
	    }
	}
	
	// Convert MultipartFile or raw image bytes to JPEG/PNG format
    public ByteBuffer convertToJpegOrPng(MultipartFile file) throws IOException {
        // Read image bytes into BufferedImage
        InputStream inputStream = new ByteArrayInputStream(file.getBytes());
        BufferedImage bufferedImage = ImageIO.read(inputStream);

        // Output the image in JPEG or PNG format
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", outputStream);  // Change "jpg" to "png" if needed

        return ByteBuffer.wrap(outputStream.toByteArray());
    }
	
}
