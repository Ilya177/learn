package com.epam.learn.handler;

import static com.epam.learn.model.RateStatus.REJECT;
import static com.epam.learn.model.TaskStatus.CLOSED;
import static com.epam.learn.model.TaskStatus.COMPLETED;
import static com.epam.learn.model.TaskStatus.RE_OPEN;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.StringUtils;
import com.epam.learn.ApiGatewayResponse;
import com.epam.learn.ObjectMapperSupplier;
import com.epam.learn.Response;
import com.epam.learn.model.RateStatus;
import com.epam.learn.model.Task;
import com.epam.learn.repository.TaskRepository;
import com.epam.learn.sqs.SQSProducer;
import com.epam.learn.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RateTaskHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final String MENTEE_SERVICE_QUEUE = System.getenv("MENTEE_SERVICE_QUEUE");

    private TaskRepository taskRepository = TaskRepository.getInstance();
    private SQSProducer sqsProducer = new SQSProducer();
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

            JsonNode body = mapper.readTree((String) input.get("body"));
            String id = ObjectMapperUtil.getValue(body, "id");
            String status = ObjectMapperUtil.getValue(body, "status");
            String rejectReason = ObjectMapperUtil.getValue(body, "rejectReason");

            Optional<Task> taskOptional = taskRepository.findById(id);
            if (!taskOptional.isPresent()) {
                Response responseBody = new Response(Collections.singletonList("Cannot find task by id"), input);
                return ApiGatewayResponse.builder()
                        .setStatusCode(404)
                        .setObjectBody(responseBody)
                        .build();
            }

            Task task = taskOptional.get();
            if (!COMPLETED.equals(task.getStatus())) {
                Response responseBody = new Response(Collections.singletonList("Invalid task status"), input);
                return ApiGatewayResponse.builder()
                        .setStatusCode(400)
                        .setObjectBody(responseBody)
                        .build();
            }

            rateTask(task, status, rejectReason);

            sqsProducer.produce(MENTEE_SERVICE_QUEUE, ObjectMapperUtil.toString(mapper, task));

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

    private boolean isRejected(String status) {
        RateStatus rateStatus = RateStatus.getByName(status);
        return REJECT.equals(rateStatus);
    }

    private List<String> validate(Map<String, Object> input) throws IOException {
        if (input == null || input.get("body") == null) {
            return Collections.singletonList("Body cannot be empty");
        }

        JsonNode body = mapper.readTree((String) input.get("body"));
        List<String> errors = new ArrayList<>();
        String id = ObjectMapperUtil.getValue(body, "id");
        if (StringUtils.isNullOrEmpty(id)) {
            errors.add("Id is required");
        }

        String status = ObjectMapperUtil.getValue(body, "status");
        if (StringUtils.isNullOrEmpty(status)) {
            errors.add("Status is required");
        } else if (RateStatus.getByName(status) == null) {
            errors.add("Invalid status");
        }

        String rejectReason = ObjectMapperUtil.getValue(body, "rejectReason");
        if (isRejected(status) && StringUtils.isNullOrEmpty(rejectReason)) {
            errors.add("Reject reason is required");
        }

        return errors;
    }

    private void rateTask(Task task, String status, String rejectReason) {
        boolean isRejected = isRejected(status);
        if (isRejected) {
            task.setStatus(RE_OPEN);
            task.setRejectReason(rejectReason);
        } else {
            task.setStatus(CLOSED);
        }
        taskRepository.save(task);
    }
}
