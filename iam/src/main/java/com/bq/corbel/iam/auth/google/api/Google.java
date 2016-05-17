package com.bq.corbel.iam.auth.google.api;

import org.springframework.social.ApiBinding;

import com.bq.corbel.iam.auth.google.api.userinfo.UserInfoOperations;

public interface Google extends ApiBinding {

    UserInfoOperations userOperations();

    String getAccessToken();
}
