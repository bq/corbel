package com.bq.corbel.resources.rem.i18n.api;

import javax.ws.rs.core.Response;

import com.bq.corbel.lib.ws.api.error.ErrorResponseFactory;
import com.bq.corbel.lib.ws.model.Error;


public class I18nErrorResponseFactory extends ErrorResponseFactory {

    public static final String LANGUAGE_HEADER_NOT_FOUND = "language_header_not_found";
    public static final String I18N_REQUIRE_ACCEPT_LANGUAGE_HEADER = "i18n require Accept-Language header";

    private static I18nErrorResponseFactory instance;

    private I18nErrorResponseFactory() {}

    public static I18nErrorResponseFactory getInstance() {
        if (instance == null) {
            instance = new I18nErrorResponseFactory();
        }
        return instance;
    }

    public Response errorNotLanguageHeader() {
        return ErrorResponseFactory.getInstance().badRequest(new Error(LANGUAGE_HEADER_NOT_FOUND, I18N_REQUIRE_ACCEPT_LANGUAGE_HEADER));
    }

}
