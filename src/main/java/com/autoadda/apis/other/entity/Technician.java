package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Technician {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "emirates_id")
	private String emiratesId;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "vat_number")
	private String vatNumber;

	@ElementCollection
    private List<String> skillSet;

    @Enumerated(EnumType.STRING)
    private Status status;

	
	@Column(name = "profile_img")
	private String profileImg;
	
	@Column(name = "insert_tms")
	private Date insertTms;
}
