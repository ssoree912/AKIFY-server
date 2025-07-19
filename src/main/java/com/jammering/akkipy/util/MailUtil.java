package com.jammering.akkipy.util;

import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class MailUtil {

    public String createVerificationEmailTemplate(String verificationCode) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/email-verification.html");
        if (inputStream == null) {
            throw new IOException("Email template 'email-verification.html' not found.");
        }
        String htmlContent = FileCopyUtils.copyToString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return htmlContent.replace("{{verificationCode}}", verificationCode);
    }
}
