package ru.t1.nour.microservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;
import ru.t1.nour.microservice.model.enums.ProductKey;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product extends AbstractPersistable<Long> {
    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "key", nullable = false)
    private ProductKey key;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @PostPersist
    public void generateProductId(){
        if(this.productId == null)
            this.productId = this.key.getValue() + this.getId();
    }
}
