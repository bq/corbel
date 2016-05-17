package com.bq.corbel.oauth.service;

import com.bq.corbel.oauth.model.Client;

/**
 * @author Alberto J. Rubio
 */
public interface MailResetPasswordService {

    void sendMailResetPassword(Client client, String userId, String email);
}
