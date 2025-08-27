package com.codewithmosh.store.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddItemToCartRequest {
    @NotBlank(message = "productId is required")
    private Long productId;
}
