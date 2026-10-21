package com.autoadda.apis.other.clients;

import com.autoadda.apis.other.request.SmsRequest;
import com.autoadda.apis.other.response.GenericResponse;
import com.autoadda.apis.other.response.UserResponse;
import com.autoadda.apis.other.util.ERole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "identity-api", url = "${clients.identity-api.url}")
public interface AuthClient {

    @GetMapping("/auth/{id}")
    UserResponse getUser(@PathVariable("id") Integer id);

    @PutMapping("/auth/update/role/{id}")
    GenericResponse updateRole(
            @PathVariable("id") Integer id,
            @RequestParam("role") ERole role
    );

    @GetMapping("/auth/byNumber/{phoneNumber}")
    ResponseEntity<UserResponse> getUserDetails(
            @PathVariable("phoneNumber") String phoneNumber
    );

    @PostMapping("/auth/send-sms")
    GenericResponse sendSms(@RequestBody SmsRequest request);
}
