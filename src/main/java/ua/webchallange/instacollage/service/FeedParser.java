package ua.webchallange.instacollage.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FeedParser {

    public List<String> getPhotosLinks(String jsonWithPhotos, int photosNumber) {
        JSONObject jsonObject = new JSONObject(jsonWithPhotos);
        JSONArray dataArray = jsonObject.getJSONArray("data");
        int finalPhotosNumber = Math.min(dataArray.length(), photosNumber);
        List<String> linksToPhotos = new ArrayList<>();
        for (int i = 0; i < finalPhotosNumber; i++) {
            linksToPhotos.add(dataArray.getJSONObject(i)
                    .getJSONObject("images")
                    .getJSONObject("standard_resolution")
                    .getString("url"));
        }
        return linksToPhotos;
    }
}
