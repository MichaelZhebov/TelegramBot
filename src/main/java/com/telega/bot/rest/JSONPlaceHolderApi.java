package com.telega.bot.rest;

import com.telega.bot.model.Employee;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface JSONPlaceHolderApi {

    @GET("/api/v1/employees")
    public Call<List<Employee>> getAllEmployees();

}
