package com.autoadda.apis.other.response;

import java.util.List;

import com.autoadda.apis.other.entity.Booking;
import com.autoadda.apis.other.entity.BookedService;
import com.autoadda.apis.other.entity.JobCard;
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
public class OsAppointmentResponse {
    private Booking appointment;
    private List<BookedService> services;
    private List<JobCard> jobcards;

}
