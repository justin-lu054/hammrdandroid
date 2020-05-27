package com.lujustin.hammrd.models;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapsApiService {
    @GET("place/nearbysearch/json?rankby=distance&type=restaurant&fields=name&keyword=fast%20food&opennow=true")
    Call<NearestOpenRestaurantList> getNearestOpenRestaurant(@Query("key") String apiKey,
                                                                   @Query("location") String latlngString);
    @GET("place/autocomplete/json")
    Call<PlaceAutoSuggestionList> getAutocompleteSuggestions(@Query("key") String apiKey,
                                                             @Query("input") String input);
    @GET("directions/json?mode=walking")
    Call<NavDirectionsList> getDirections(@Query("key") String apiKey,
                                          @Query("origin") String origin,
                                          @Query("destination") String destination);

}
