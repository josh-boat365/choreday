package org.group45.choreday.models;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModel {

    @NonNull
    @Column(unique = true, name = "student_id")
    private String studentId;

    @NonNull
    @Column(name = "full_name")
    private String fullName;

    @NonNull
    private String password;

}
