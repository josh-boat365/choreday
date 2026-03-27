package org.group45.choreday.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chores")
public class ChoreModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String activityName;
    private String city;

    @ManyToOne
    @JoinColumn(name = "weather_id")
    private WeatherRecord weather;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserModel user;

    private LocalDateTime createdAt;
}
