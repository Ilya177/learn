package com.epam.learn;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import lombok.Getter;

public class AwsClientSupplier {

    @Getter
    private static final AwsClientSupplier instance = new AwsClientSupplier();

    @Getter
    private final AmazonDynamoDB dynamoDB;

    @Getter
    private final AmazonKinesis kinesis;

    @Getter
    private final AmazonSimpleEmailService simpleEmailService;

    @Getter
    private final AmazonSQS sqs;

    private AwsClientSupplier() {
        dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
        kinesis = AmazonKinesisClientBuilder.standard().build();
        simpleEmailService = AmazonSimpleEmailServiceClientBuilder.standard().build();
        sqs = AmazonSQSClientBuilder.standard().build();
    }
}
