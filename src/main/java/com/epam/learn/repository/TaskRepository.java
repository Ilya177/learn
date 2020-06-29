package com.epam.learn.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.epam.learn.AwsClientSupplier;
import com.epam.learn.model.Task;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

public class TaskRepository {

    private static final String TASK_TABLE_NAME = System.getenv("TASK_TABLE_NAME");

    @Getter
    private static final TaskRepository instance = new TaskRepository();

    private final DynamoDBMapper mapper;

    private TaskRepository() {
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(TASK_TABLE_NAME))
                .build();
        mapper = new DynamoDBMapper(AwsClientSupplier.getInstance().getDynamoDB(), mapperConfig);
    }

    public Optional<Task> findById(String id) {
        Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put(":id", new AttributeValue().withS(id));

        DynamoDBQueryExpression<Task> queryExp = new DynamoDBQueryExpression<Task>()
                .withKeyConditionExpression("id = :id")
                .withExpressionAttributeValues(attributes);

        PaginatedQueryList<Task> result = mapper.query(Task.class, queryExp);
        if (!result.isEmpty()) {
            return Optional.of(result.get(0));
        }

        return Optional.empty();
    }

    public void save(Task task) {
        mapper.save(task);
    }
}
