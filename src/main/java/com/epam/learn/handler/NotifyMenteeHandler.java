package com.epam.learn.handler;

import static com.epam.learn.model.TaskStatus.RE_OPEN;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.epam.learn.ObjectMapperSupplier;
import com.epam.learn.model.Task;
import com.epam.learn.ses.EmailService;
import com.epam.learn.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NotifyMenteeHandler implements RequestHandler<SQSEvent, Void> {

    private static final String MENTEE_EMAIL = System.getenv("MENTEE_EMAIL");
    private static final String LEARN_SYSTEM_EMAIL = System.getenv("LEARN_SYSTEM_EMAIL");

    private EmailService emailService = new EmailService();
    private ObjectMapper mapper = ObjectMapperSupplier.getInstance().getMapper();

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        sqsEvent.getRecords()
                .stream()
                .map(SQSMessage::getBody)
                .map(json -> ObjectMapperUtil.toObject(mapper, json, Task.class))
                .forEach(this::notifyMentee);

        return null;
    }

    private void notifyMentee(Task task) {
        String subject = "Status of task has been changed to: " + task.getStatus().getName();
        String body = String.format("<p>id: %s</p><p>name: %s</p><p>description: %s</p>", task.getId(), task.getName(), task.getDescription());
        if (RE_OPEN.equals(task.getStatus())) {
            body += String.format("<p>reject reason: %s</p>", task.getRejectReason());
        }
        emailService.sendEmail(LEARN_SYSTEM_EMAIL, MENTEE_EMAIL, subject, body);
    }
}
