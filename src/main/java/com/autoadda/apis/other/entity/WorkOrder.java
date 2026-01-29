package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class WorkOrder {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne
    private Booking appointment;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL)
    private List<OsJobCard> jobCards;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdDate;


}
