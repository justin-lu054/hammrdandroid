package com.lujustin.hammrd.models;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TwilioInterface {
    @POST("text")
    Call<Void> sendSMS(@Body HammrdTwilioData bodyData);
}
