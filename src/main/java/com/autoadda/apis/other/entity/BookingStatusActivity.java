package com.autoadda.apis.other.entity;

import java.time.LocalDate;

import com.autoadda.apis.other.util.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "booking_status_activity")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingStatusActivity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "booking_id")
    private Integer bookingId;

    @Column(name = "status")
    private Status status;

    @Column(name = "comments")
    private String comments;

    @Column(name = "insert_tms")
    private LocalDate insertTms;

    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "member_type")
    private String memberType;

}
