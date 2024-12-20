package com.blog_app.services;

import java.io.IOException;

import org.apache.mahout.cf.taste.model.DataModel;

public interface MahoutDataLoaderService {

	DataModel createDataModel()throws IOException;
}
