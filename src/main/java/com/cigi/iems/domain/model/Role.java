package com.cigi.iems.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    private RoleName name;

    public enum RoleName {
        ROLE_NOT_SPECIFIED,
        ROLE_ADMIN,
        ROLE_USER,
        ROLE_HR,
        ROLE_LAB,
        ROLE_QUALITY,
        ROLE_SALE
    }
}
