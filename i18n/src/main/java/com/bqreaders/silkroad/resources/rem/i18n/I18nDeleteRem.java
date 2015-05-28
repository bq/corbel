package com.bqreaders.silkroad.resources.rem.i18n;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;

import com.bqreaders.silkroad.resources.rem.i18n.api.I18nErrorResponseFactory;
import com.bqreaders.silkroad.resources.rem.i18n.model.I18n;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;

/**
 * Created by Francisco Sanchez on 15/04/15.
 */
public class I18nDeleteRem extends I18nBaseRem {

	public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters,
			Optional<I18n> optionalEntity) {
		return Optional.ofNullable(getLanguage(parameters.getHeaders())).map(language -> {
			id.setId(language + ":" + id.getId());
			return getJsonRem(type, HttpMethod.DELETE).resource(type, id, parameters, Optional.empty());
		}).orElse(I18nErrorResponseFactory.getInstance().errorNotLanguageHeader());

	}

	public Class<I18n> getType() {
		return I18n.class;
	}

}
