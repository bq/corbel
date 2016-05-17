package com.bq.corbel.oauth.service;

import com.bq.corbel.oauth.model.Client;

/**
 * @author Alberto J. Rubio
 */
public interface MailValidationService {

    void sendMailValidation(Client client, String userId, String email);

}
