package com.bq.corbel.oauth.service;

import com.bq.corbel.oauth.model.Client;

/**
 * @author Francisco Sanchez
 */
public interface MailChangePasswordService {

    void sendMailChangePassword(Client client, String username, String email);
}
