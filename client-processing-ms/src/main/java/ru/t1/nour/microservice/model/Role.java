package ru.t1.nour.microservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;
import ru.t1.nour.microservice.model.enums.RoleEnum;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles")
public class Role extends AbstractPersistable<Long> {

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleEnum name;
}
