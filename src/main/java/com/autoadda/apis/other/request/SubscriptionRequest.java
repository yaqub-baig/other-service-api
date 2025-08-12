package com.autoadda.apis.other.request;

import lombok.Data;

@Data
public class SubscriptionRequest {
    private Integer ownerId;
    private Integer planId;
}
