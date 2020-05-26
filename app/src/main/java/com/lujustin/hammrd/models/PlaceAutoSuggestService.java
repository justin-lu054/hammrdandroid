package com.lujustin.hammrd.models;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlaceAutoSuggestService {

    @GET("place/autocomplete/json")
    Call<PlaceAutoSuggestionList> getAutocompleteSuggestions(@Query("key") String apiKey,
                                                         @Query("input") String input);

}
