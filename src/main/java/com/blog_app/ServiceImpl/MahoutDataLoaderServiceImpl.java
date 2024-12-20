package com.blog_app.ServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.classifier.df.data.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog_app.entities.UserInteraction;
import com.blog_app.repositories.UserInteractionRepo;
import com.blog_app.services.MahoutDataLoaderService;
@Service
public class MahoutDataLoaderServiceImpl implements MahoutDataLoaderService{

	@Autowired
	private UserInteractionRepo userInteractionRepo;
	
	 // Map to store user preferences
    Map<Long, GenericUserPreferenceArray> data = new HashMap<>();
    
	@Override
	 public DataModel createDataModel() throws IOException {
        // Use FastByIDMap to store preferences by user ID
        FastByIDMap<PreferenceArray> data = new FastByIDMap<>();

        // Fetch all user interactions
        List<UserInteraction> interactions = userInteractionRepo.findAll();

        // Group interactions by user
        Map<Integer, List<UserInteraction>> interactionsByUser = interactions.stream()
                .collect(Collectors.groupingBy(interaction -> interaction.getUser().getId()));

        // Loop over each user and their interactions
        for (Entry<Integer, List<UserInteraction>> entry : interactionsByUser.entrySet()) {
            long userId = entry.getKey().longValue();
            List<UserInteraction> userInteractions = entry.getValue();

            // List to hold preferences for this user
            List<Preference> preferences = new ArrayList<>();
            for (UserInteraction interaction : userInteractions) {
                preferences.add(new GenericPreference(userId, interaction.getPost().getPostId(), (float) interaction.getInteractionScore()));
            }

            // Create and add the user preferences to FastByIDMap
            PreferenceArray prefArray = new GenericUserPreferenceArray(preferences);
            data.put(userId, prefArray);
        }
        // Return the data model
        return new GenericDataModel(data);
    }

}
