package com.fip.appointmentapi.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentCsvRecord
{

    @CsvBindByName(column = "name", required = true)
    private String name;

    @CsvBindByName(column = "matric_number", required = true)
    private String matricNumber;

    @CsvBindByName(column = "gender", required = true)
    private String gender;

    @CsvBindByName(column = "year_of_study", required = true)
    private int yearOfStudy;
}