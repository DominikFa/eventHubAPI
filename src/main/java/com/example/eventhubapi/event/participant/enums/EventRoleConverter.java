package com.example.eventhubapi.event.participant.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter to convert EventRole enum to and from a String
 * representation in the database.
 */
@Converter(autoApply = true)
public class EventRoleConverter implements AttributeConverter<EventRole, String> {

    @Override
    public String convertToDatabaseColumn(EventRole eventRole) {
        if (eventRole == null) {
            return null;
        }
        return eventRole.getValue();
    }

    @Override
    public EventRole convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return EventRole.fromValue(value);
    }
}