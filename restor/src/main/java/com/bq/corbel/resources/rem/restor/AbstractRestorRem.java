package com.bq.corbel.resources.rem.restor;

import java.io.InputStream;

import com.bq.corbel.resources.rem.request.RequestParameters;
import com.bq.corbel.resources.rem.BaseRem;
import com.bq.corbel.resources.rem.dao.RestorDao;

/**
 * @author Alberto J. Rubio
 */
public abstract class AbstractRestorRem extends BaseRem<InputStream> {

	protected final RestorDao dao;

	public AbstractRestorRem(RestorDao dao) {
		this.dao = dao;
	}

	public String getMediaType(RequestParameters<?> parameters) {
		return parameters.getAcceptedMediaTypes().get(0).toString();
	}

	@Override
	public Class<InputStream> getType() {
		return InputStream.class;
	}
}
