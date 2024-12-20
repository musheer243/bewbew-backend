package com.blog_app.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import com.blog_app.exceptions.PostUpdateDataNotFoundException;

public interface FIleServiceMedia {

	List<String> uploadMedia(List<MultipartFile> files) throws IOException;

	List<InputStream> getResource(List<String> fileNames) throws FileNotFoundException;

	void deleteFileFromS3(List<String> filename);

	void deletePostFiles(List<String> fileNames);

	void deleteLocalFiles(List<String> fileNames);
}
