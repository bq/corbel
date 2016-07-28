package com.bq.corbel.iam.service;

public interface MailResetPasswordService {

    void sendMailResetPassword(String clientId, String userId, String username, String email, String domainId);
}
