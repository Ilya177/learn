package com.epam.learn.handler;

import static com.epam.learn.model.TaskStatus.COMPLETED;
import static com.epam.learn.model.TaskStatus.OPEN;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.Record;
import com.epam.learn.ObjectMapperSupplier;
import com.epam.learn.model.Task;
import com.epam.learn.ses.EmailService;
import com.epam.learn.sqs.SQSProducer;
import com.epam.learn.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NotifyAllHandler implements RequestHandler<KinesisEvent, Void> {

    private static final String MENTEE_EMAIL = System.getenv("MENTEE_EMAIL");
    private static final String LEARN_SYSTEM_EMAIL = System.getenv("LEARN_SYSTEM_EMAIL");
    private static final String MENTOR_SERVICE_QUEUE = System.getenv("MENTOR_SERVICE_QUEUE");

    private EmailService emailService = new EmailService();
    private SQSProducer sqsProducer = new SQSProducer();
    private ObjectMapper mapper = ObjectMapperSupplier.getInstance().getMapper();

    @Override
    public Void handleRequest(KinesisEvent event, Context context) {
        List<Task> tasks = event.getRecords()
                .stream()
                .map(KinesisEventRecord::getKinesis)
                .map(Record::getData)
                .map(data -> ObjectMapperUtil.toObject(mapper, data, Task.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        tasks.stream()
                .filter(task -> OPEN.equals(task.getStatus()))
                .forEach(this::notifyMentee);

        tasks.stream()
                .filter(task -> COMPLETED.equals(task.getStatus()))
                .forEach(this::notifyMentor);

        return null;
    }

    private void notifyMentee(Task task) {
        String subject = "New task has been assigned";
        String body = String.format("<p>id: %s</p><p>name: %s</p><p>description: %s</p>", task.getId(), task.getName(), task.getDescription());
        emailService.sendEmail(LEARN_SYSTEM_EMAIL, MENTEE_EMAIL, subject, body);
    }

    private void notifyMentor(Task task) {
        sqsProducer.produce(MENTOR_SERVICE_QUEUE, ObjectMapperUtil.toString(mapper, task));
    }
}
