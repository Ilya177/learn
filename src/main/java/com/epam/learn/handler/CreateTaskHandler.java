package com.epam.learn.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.StringUtils;
import com.epam.learn.ApiGatewayResponse;
import com.epam.learn.ObjectMapperSupplier;
import com.epam.learn.Response;
import com.epam.learn.kinesis.KinesisProducer;
import com.epam.learn.model.Task;
import com.epam.learn.model.TaskStatus;
import com.epam.learn.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.epam.learn.util.ObjectMapperUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CreateTaskHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final String TASK_STREAM_NAME = System.getenv("TASK_STREAM_NAME");

    private TaskRepository taskRepository = TaskRepository.getInstance();
    private KinesisProducer kinesisProducer = new KinesisProducer();
    private ObjectMapper mapper = ObjectMapperSupplier.getInstance().getMapper();

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        log.info("received: {}", input);

        try {
            List<String> errors = validate(input);
            if (!errors.isEmpty()) {
                Response responseBody = new Response(errors, input);
                return ApiGatewayResponse.builder()
                        .setStatusCode(400)
                        .setObjectBody(responseBody)
                        .build();
            }

            Task task = createTask(input);

            ByteBuffer data = ObjectMapperUtil.toByteBuffer(mapper, task);
            kinesisProducer.produce(TASK_STREAM_NAME, task.getId(), data);

            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(task)
                    .build();
        } catch (IOException ex) {
            log.error("Invalid body: " + ex);
            Response responseBody = new Response(Collections.singletonList("Invalid body"), input);
            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setObjectBody(responseBody)
                    .build();
        } catch (Exception ex) {
            log.error("Error in saving task: " + ex);
            Response responseBody = new Response(Collections.singletonList("Error in saving task"), input);
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setObjectBody(responseBody)
                    .build();
        }
    }

    private List<String> validate(Map<String, Object> input) throws IOException {
        if (input == null || input.get("body") == null) {
            return Collections.singletonList("Body cannot be empty");
        }

        JsonNode body = mapper.readTree((String) input.get("body"));
        List<String> errors = new ArrayList<>();
        String name = ObjectMapperUtil.getValue(body, "name");
        if (StringUtils.isNullOrEmpty(name)) {
            errors.add("Name is required");
        }

        String description = ObjectMapperUtil.getValue(body, "description");
        if (StringUtils.isNullOrEmpty(description)) {
            errors.add("Description is required");
        }

        return errors;
    }

    private Task createTask(Map<String, Object> input) throws IOException {
        JsonNode body = mapper.readTree((String) input.get("body"));
        String name = ObjectMapperUtil.getValue(body, "name");
        String description = ObjectMapperUtil.getValue(body, "description");

        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setStatus(TaskStatus.OPEN);
        task.setCreated(Instant.now());
        taskRepository.save(task);

        return task;
    }
}
