package com.epam.learn.kinesis;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.epam.learn.AwsClientSupplier;
import java.nio.ByteBuffer;

public class KinesisProducer {

    private final AmazonKinesis kinesis;

    public KinesisProducer() {
        this.kinesis = AwsClientSupplier.getInstance().getKinesis();
    }

    public void produce(String streamName, String partitionKey, ByteBuffer data) {
        PutRecordRequest record = new PutRecordRequest()
                .withStreamName(streamName)
                .withPartitionKey(partitionKey)
                .withData(data);
        kinesis.putRecord(record);
    }
}
