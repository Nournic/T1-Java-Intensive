package ru.t1.nour.microservice.model.dto.request;

import lombok.Data;
import ru.t1.nour.microservice.model.enums.Key;

@Data
public class ProductUpdateRequest {
    String name;
    Key key;
}
