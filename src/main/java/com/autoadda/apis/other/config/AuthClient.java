package com.autoadda.apis.other.config;

import com.autoadda.apis.other.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/{id}")
    UserResponse getUser(@PathVariable("id") Integer id);

}
