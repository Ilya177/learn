package com.epam.learn.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.epam.learn.AwsClientSupplier;

public class SQSProducer {

    private final AmazonSQS sqs;

    public SQSProducer() {
        sqs = AwsClientSupplier.getInstance().getSqs();
    }

    public void produce(String queueUrl, String messageBody) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, messageBody);
        sqs.sendMessage(sendMessageRequest);
    }
}
