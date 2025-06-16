package com.example.eventhubapi.invitation.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter to convert InvitationStatus enum to and from a String
 * representation in the database.
 */
@Converter(autoApply = true)
public class InvitationStatusConverter implements AttributeConverter<InvitationStatus, String> {

    @Override
    public String convertToDatabaseColumn(InvitationStatus status) {
        if (status == null) {
            return null;
        }
        return status.getValue();
    }

    @Override
    public InvitationStatus convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return InvitationStatus.fromValue(value);
    }
}