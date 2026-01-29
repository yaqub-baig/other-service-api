package com.autoadda.apis.other.response;

import java.util.List;

import com.autoadda.apis.other.entity.Booking;
import com.autoadda.apis.other.entity.OsAppointmentServices;
import com.autoadda.apis.other.entity.OsJobCard;
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
    private List<OsAppointmentServices> services;
    private List<OsJobCard> jobcards;

}
