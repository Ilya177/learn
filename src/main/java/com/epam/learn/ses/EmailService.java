package com.epam.learn.ses;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.epam.learn.AwsClientSupplier;
import java.util.Collections;

public class EmailService {

    private final AmazonSimpleEmailService simpleEmailService;

    public EmailService() {
        simpleEmailService = AwsClientSupplier.getInstance().getSimpleEmailService();
    }

    public void sendEmail(String source, String destination, String subject, String message) {
        SendEmailRequest sendEmailRequest =
                new SendEmailRequest()
                        .withSource(source)
                        .withDestination(new Destination(Collections.singletonList(destination)))
                        .withMessage(new Message()
                                .withSubject(new Content().withData(subject))
                                .withBody(new Body().withHtml(new Content().withData(message))));

        simpleEmailService.sendEmail(sendEmailRequest);
    }
}
