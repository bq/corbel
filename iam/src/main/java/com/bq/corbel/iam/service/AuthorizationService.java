package com.bq.corbel.iam.service;

import com.bq.corbel.iam.auth.OauthParams;
import com.bq.corbel.iam.exception.*;
import com.bq.corbel.iam.model.TokenGrant;

/**
 * @author Alexander De Leon
 */
public interface AuthorizationService {

    TokenGrant authorize(String assertion) throws UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException,
            MissingBasicParamsException;

    TokenGrant authorize(String assertion, OauthParams params) throws UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException;

}
