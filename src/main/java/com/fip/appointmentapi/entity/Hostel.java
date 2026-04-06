package com.fip.appointmentapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hostels")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Hostel
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    public Hostel(String name, String location, Gender gender)
    {
        this.name = name;
        this.location = location;
        this.gender = gender;
    }
}