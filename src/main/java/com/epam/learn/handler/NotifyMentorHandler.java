package com.epam.learn.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.util.StringUtils;
import com.epam.learn.ObjectMapperSupplier;
import com.epam.learn.model.Task;
import com.epam.learn.ses.EmailService;
import com.epam.learn.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NotifyMentorHandler implements RequestHandler<SQSEvent, Void> {

    private static final String MENTOR_EMAIL = System.getenv("MENTOR_EMAIL");
    private static final String LEARN_SYSTEM_EMAIL = System.getenv("LEARN_SYSTEM_EMAIL");

    private EmailService emailService = new EmailService();
    private ObjectMapper mapper = ObjectMapperSupplier.getInstance().getMapper();

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        sqsEvent.getRecords()
                .stream()
                .map(SQSMessage::getBody)
                .map(json -> ObjectMapperUtil.toObject(mapper, json, Task.class))
                .forEach(this::notifyMentor);

        return null;
    }

    private void notifyMentor(Task task) {
        String subject = "Task has been completed";
        String body = String.format("<p>id: %s</p><p>name: %s</p><p>description: %s</p><p>result: %s</p>", task.getId(), task.getName(),
                task.getDescription(), task.getResult());
        if (!StringUtils.isNullOrEmpty(task.getRejectReason())) {
            body += String.format("<p>reject reason: %s</p>", task.getRejectReason());
        }
        emailService.sendEmail(LEARN_SYSTEM_EMAIL, MENTOR_EMAIL, subject, body);
    }
}
