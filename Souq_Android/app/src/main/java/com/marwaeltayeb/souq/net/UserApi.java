package com.marwaeltayeb.souq.net;

import com.marwaeltayeb.souq.model.UserModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserApi {

    @POST("register")
    Call<ResponseBody> insertUser(@Body UserModel user);


    @GET("login")
    Call<ResponseBody> logInUser(@Query("email") String email, @Query("password") String password);
}
