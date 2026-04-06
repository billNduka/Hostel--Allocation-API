package com.fip.appointmentapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String matricNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Min(1) @Max(7)
    @Column(nullable = false)
    private int yearOfStudy;

    @ElementCollection
    @CollectionTable(
            name = "student_preferences",
            joinColumns = @JoinColumn(name = "student_id")
    )
    @OrderColumn(name = "preference_rank")
    private List<StudentPreference> preferences = new ArrayList<>();

    public Student(String name, String matricNumber, Gender gender, int yearOfStudy) {
        this.name = name;
        this.matricNumber = matricNumber;
        this.gender = gender;
        this.yearOfStudy = yearOfStudy;
    }

    public boolean hasPreferences() {
        return preferences != null && !preferences.isEmpty();
    }
}