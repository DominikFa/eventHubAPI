package com.example.eventhubapi.event.media.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter to convert MediaUsage enum to and from a String
 * representation in the database.
 */
@Converter(autoApply = true)
public class MediaUsageConverter implements AttributeConverter<MediaUsage, String> {

    @Override
    public String convertToDatabaseColumn(MediaUsage mediaUsage) {
        if (mediaUsage == null) {
            return null;
        }
        return mediaUsage.getValue();
    }

    @Override
    public MediaUsage convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return MediaUsage.fromValue(value);
    }
}