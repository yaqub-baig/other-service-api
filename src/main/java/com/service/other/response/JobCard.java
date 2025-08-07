package com.service.other.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.service.other.util.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobCard {

	private Integer id;

	private LocalTime serviceStartTime;

	private LocalTime serviceEndTime;

	private Status status;

	private LocalDate insert_tms;

	private AppointmentDetails appointment;

	private GarageTechnician technician;

	private AppointmentServices service;

	private Garage garage;

	private String comments;

}
