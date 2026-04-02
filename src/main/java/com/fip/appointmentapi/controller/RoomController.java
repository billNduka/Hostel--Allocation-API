package com.fip.appointmentapi.controller;

import com.fip.appointmentapi.entity.Hostel;
import com.fip.appointmentapi.entity.Room;
import com.fip.appointmentapi.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController 
{
	private final RoomService roomService;

	public RoomController(RoomService roomService)
	{
		this.roomService = roomService;
	}

}