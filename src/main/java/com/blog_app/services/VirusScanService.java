package com.blog_app.services;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface VirusScanService {

	boolean scanFile(MultipartFile file) throws IOException;
}
