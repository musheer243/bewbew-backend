package com.blog_app.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FileService {

	String uploadImage(MultipartFile file) throws IOException;
	InputStream getResource(String fileName) throws FileNotFoundException;
	void deleteFileFromS3(String filename);
	void deleteLocalFile(String fileName);
	void deleteUserFile(String profilepic);
}