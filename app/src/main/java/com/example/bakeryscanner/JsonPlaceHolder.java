package com.example.bakeryscanner;

import com.example.bakeryscanner.model.Customer;
import com.example.bakeryscanner.model.CustomerCart;
import com.example.bakeryscanner.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface JsonPlaceHolder {
    @GET("posts")
    Call<List<Product>> getPosts();

    @GET("/cakeProduct/loadUserCash.php")
    Call<List<Customer>> getUserCash(
            @Query("method") String method,
            @Query("id") String id);

    @GET("/cakeProduct/displayUserOrderDetails.php")
    Call<List<CustomerCart>> getUserOrderDetails(
            @Query("method") String method,
            @Query("id") String id);



}
