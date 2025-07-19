package com.jammering.akkipy.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.jammering.akkipy.util.MailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final MailUtil mailUtil;



    public String sendVerificationEmail(String toEmail) {
        String verificationCode = generateVerificationCode();
        String subject = "Akkipy 회원가입 인증 코드";

        try {
            String htmlBody = mailUtil.createVerificationEmailTemplate(verificationCode);
            sendEmail(toEmail, subject, htmlBody);
            log.info("인증 이메일 발송 완료: {}", toEmail);
            return verificationCode;
        } catch (IOException e) {
            log.error("이메일 템플릿을 찾을 수 없습니다.", e);
            throw new RuntimeException("이메일 템플릿을 찾을 수 없습니다.");
        } catch (AmazonSimpleEmailServiceException e) {
            log.error("이메일 발송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(to))
                .withMessage(new Message()
                        .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBody)))
                        .withSubject(new Content().withCharset("UTF-8").withData(subject)))
                .withSource("info@jammering.com");

        amazonSimpleEmailService.sendEmail(request);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6자리 인증 코드
        return String.valueOf(code);
    }
}