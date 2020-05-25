package com.lujustin.hammrd.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class PlaceApi {

    private final String baseURL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    private String apiKey;

    public PlaceApi(String apiKey) {
        this.apiKey = apiKey;
    }

    public ArrayList<String> autoComplete(String input) {
        ArrayList<String> arrayList = new ArrayList();

        HttpURLConnection connection = null;
        StringBuilder jsonResult = new StringBuilder();
        try {
            StringBuilder queryBuilder = new StringBuilder(baseURL);
            queryBuilder.append("input=" + input);
            queryBuilder.append("&key=" + apiKey);
            URL requestUrl = new URL(queryBuilder.toString());
            connection = (HttpURLConnection) requestUrl.openConnection();
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

            int read;
            char[] buffer = new char[1024];
            while ((read=inputStreamReader.read(buffer)) != -1) {
                jsonResult.append(buffer, 0, read);
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonResult.toString());
            JSONArray predictions = jsonObject.getJSONArray("predictions");
            for (int i = 0; i < predictions.length(); i++) {
                arrayList.add(predictions.getJSONObject(i).getString("description"));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;

    }
}
