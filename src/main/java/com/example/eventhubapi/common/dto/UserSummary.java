package com.example.eventhubapi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A simplified DTO representing a user, used for embedding in other DTOs.
 * This prevents circular dependencies and reduces payload size.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {
    private Long id;
    private String name;
    private String profileImageUrl;
}