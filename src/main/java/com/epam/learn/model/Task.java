package com.epam.learn.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
@DynamoDBTable(tableName = "PLACEHOLDER_TASK_TABLE_NAME")
public class Task {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    @DynamoDBAutoGeneratedKey
    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBAttribute(attributeName = "name")
    private String name;

    @DynamoDBAttribute(attributeName = "description")
    private String description;

    @DynamoDBTypeConvertedEnum
    @DynamoDBAttribute(attributeName = "status")
    private TaskStatus status;

    @DynamoDBAttribute(attributeName = "created")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN, timezone = "UTC")
    private Instant created;

    @DynamoDBAttribute(attributeName = "result")
    private String result;

    @DynamoDBAttribute(attributeName = "fulfillment_date")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN, timezone = "UTC")
    private Instant fulfillmentDate;

    @DynamoDBAttribute(attributeName = "reject_reason")
    private String rejectReason;


    public static class InstantConverter implements DynamoDBTypeConverter<String, Instant> {

        @Override
        public String convert(Instant instant) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
                    .withZone(ZoneId.systemDefault());
            return dateTimeFormatter.format(instant);
        }

        @Override
        public Instant unconvert(String string) {
            return Instant.parse(string);
        }
    }
}