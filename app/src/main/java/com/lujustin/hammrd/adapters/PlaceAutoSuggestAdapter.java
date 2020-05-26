package com.lujustin.hammrd.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.lujustin.hammrd.R;
import com.lujustin.hammrd.models.MapsApiService;
import com.lujustin.hammrd.models.PlaceAutoSuggestionList;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceAutoSuggestAdapter extends ArrayAdapter implements Filterable {



    private ArrayList<String> results;

    private int resource;
    private Context context;
    private final String baseURL = "https://maps.googleapis.com/maps/api/";

    public PlaceAutoSuggestAdapter(Context context, int resId) {
        super(context, resId);
        this.context = context;
        this.resource = resId;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public String getItem(int pos) {
        return results.get(pos);
    }

    private Call<PlaceAutoSuggestionList> getPlaceSuggestions(String input) {
        Call<PlaceAutoSuggestionList> placeSuggestionsCall = null;
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseURL)
                                                .addConverterFactory(GsonConverterFactory.create())
                                                .build();

        MapsApiService mapsApiService = retrofit.create(MapsApiService.class);

        try {
            String apiKey = context.getString(R.string.GOOGLE_API_KEY);
            placeSuggestionsCall = mapsApiService.getAutocompleteSuggestions(apiKey, input);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return placeSuggestionsCall;
    }

    @Override
    public Filter getFilter() {
        final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    try {
                        //this is executed on a background thread, we can use blocking operations
                        //add a delay of 1000 ms
                        Thread.sleep(1000);
                        results = getPlaceSuggestions(constraint.toString())
                                .execute()
                                .body()
                                .getDescriptionList();
                        filterResults.values = results;
                        filterResults.count = results.size();
                    }
                    catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

}
