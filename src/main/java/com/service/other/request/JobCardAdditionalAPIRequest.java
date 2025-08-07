package com.service.other.request;

import com.service.other.util.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JobCardAdditionalAPIRequest {
	private Integer requestId;
	private Integer jobcardId;
	private Integer garageId;
	private Integer technicianId;
	private Integer customerId;
	private Status status;
	private String requestType;
	private String comments;
}
