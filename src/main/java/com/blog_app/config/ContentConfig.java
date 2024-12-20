package com.blog_app.config;

import javax.print.attribute.standard.Media;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.media.MediaType;

@Configuration
public class ContentConfig implements WebMvcConfigurer{

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {

		configurer.favorParameter(true)
		.parameterName("MediaType")
		.defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON)
		.mediaType("json", org.springframework.http.MediaType.APPLICATION_JSON)
		.mediaType("xml", org.springframework.http.MediaType.APPLICATION_ATOM_XML);
	}

	
}
