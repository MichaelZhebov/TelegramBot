package com.telega.bot.rest;

import com.telega.bot.model.Employee;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface JSONPlaceHolderApi {

    @GET("/api/v1/employees")
    public Call<List<Employee>> getAllEmployees();

    @GET("/api/v1/employees/{id}")
    public Call<Employee> getEmployee(@Path("id") long id);

    @POST("/api/v1/employees")
    public Call<Employee> addEmployee(@Body Employee employee);

    @PUT("/api/v1/employees/{id}")
    public Call<Employee> updateEmployee(@Path("id") long id, @Body Employee employee);

    @DELETE("/api/v1/employees/{id}")
    public Call<Employee> deleteEmployee(@Path("id") long id);
}
