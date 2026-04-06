package com.fip.appointmentapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Room
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @NotBlank
    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer capacity;

    @Min(0)
    @Column(nullable = false)
    private int occupied = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    public Room(Hostel hostel, String roomNumber, int capacity, Gender gender)
    {
        this.hostel = hostel;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.gender = gender;
    }

    public int availableSpaces()
    {
        return capacity - occupied;
    }

    public boolean isFull()
    {
        return occupied >= capacity;
    }

    public Gender getGender()
    {
        return hostel.getGender();
    }
}