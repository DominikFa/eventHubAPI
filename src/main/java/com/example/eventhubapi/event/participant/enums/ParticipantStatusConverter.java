package com.example.eventhubapi.event.participant.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter to convert ParticipantStatus enum to and from a String
 * representation in the database.
 */
@Converter(autoApply = true)
public class ParticipantStatusConverter implements AttributeConverter<ParticipantStatus, String> {

    @Override
    public String convertToDatabaseColumn(ParticipantStatus status) {
        if (status == null) {
            return null;
        }
        return status.getValue();
    }

    @Override
    public ParticipantStatus convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return ParticipantStatus.fromValue(value);
    }
}