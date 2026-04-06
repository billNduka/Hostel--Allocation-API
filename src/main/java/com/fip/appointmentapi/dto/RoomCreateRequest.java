package com.fip.appointmentapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomCreateRequest
{
    private Long hostelId;
    private String roomNumber;
    private Integer capacity;
}