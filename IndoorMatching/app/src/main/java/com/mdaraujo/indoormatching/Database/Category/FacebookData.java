package com.mdaraujo.indoormatching.Database.Category;

import android.app.Application;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.mdaraujo.indoormatching.Database.AppRepository;
import com.mdaraujo.indoormatching.Database.Item.Item;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mdaraujo.commonlibrary.CommonParams.FACEBOOK_CATEGORY_ID;

public class FacebookData {

    private final static String TAG = "FB_DATA";
    private final String FACEBOOK_CATEGORY_DESCRIPTION = "User's Facebook Interests";

    private String userId;
    private AppRepository appRepository;

    public FacebookData(String userId, Application application) {
        this.userId = userId;
        this.appRepository = new AppRepository(application);
    }

    public void updateFacebookData() {

        String[] fbFields = {"likes", "favorite_teams", "favorite_athletes", "television"};
        List<Item> facebookItems = new ArrayList<>();

        GraphRequest request = (GraphRequest) GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                JSONObject fieldJSONObject, tmpObject;
                JSONArray jsonArray;

                try {
                    for (String field : fbFields) {
                        if (object.has(field)) {
                            fieldJSONObject = object.optJSONObject(field);
                            if (fieldJSONObject != null && fieldJSONObject.has("data")) {
                                jsonArray = fieldJSONObject.getJSONArray("data");
                            } else {
                                jsonArray = object.optJSONArray(field);
                            }

                            for (int i = 0; i < jsonArray.length(); i++) {
                                tmpObject = jsonArray.getJSONObject(i);
                                facebookItems.add(new Item(tmpObject.getLong("id"), tmpObject.getString("name"), FACEBOOK_CATEGORY_ID));
                            }

                        }
                    }

                    appRepository.updateFacebookAsyncTask(facebookItems, userId);

                } catch (
                        Exception e) {
                    e.printStackTrace();
                }

            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", arrayToString(fbFields));
        request.setParameters(parameters);
        request.executeAsync();
    }

    public static String arrayToString(String[] array) {
        StringBuilder sb = new StringBuilder();
        for (int strPos = 0; strPos < array.length; strPos++) {
            sb.append(array[strPos]);
            if (strPos != array.length - 1)
                sb.append(",");
        }

        return sb.toString();
    }
}
