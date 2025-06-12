package com.example.eventhubapi.notification.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NotificationStatusConverter implements AttributeConverter<NotificationStatus, String> {

    @Override
    public String convertToDatabaseColumn(NotificationStatus status) {
        if (status == null) {
            return null;
        }
        return status.getValue();
    }

    @Override
    public NotificationStatus convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return NotificationStatus.fromValue(value);
    }
}