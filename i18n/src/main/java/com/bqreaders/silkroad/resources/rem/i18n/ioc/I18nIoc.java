package com.bqreaders.silkroad.resources.rem.i18n.ioc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import com.bq.oss.lib.config.ConfigurationIoC;
import com.bq.oss.corbel.resources.rem.Rem;
import com.bqreaders.silkroad.resources.rem.i18n.I18nDeleteRem;
import com.bqreaders.silkroad.resources.rem.i18n.I18nGetRem;
import com.bqreaders.silkroad.resources.rem.i18n.I18nPutRem;
import com.google.gson.Gson;

/**
 * Created by Francisco Sanchez on 15/04/15.
 */

@Configuration
// import configuration mechanism
@Import({ ConfigurationIoC.class })
public class I18nIoc {

	@Bean(name = I18nRemNames.I18N_GET)
	public Rem getI18nGetRem() throws Exception {
		return new I18nGetRem();
	}

	@Bean(name = I18nRemNames.I18N_PUT)
	public Rem getI18nPutRem(Gson gson) throws Exception {
		return new I18nPutRem(gson);
	}

	@Bean(name = I18nRemNames.I18N_DELETE)
	public Rem getI18nDeleteRem(Gson gson) throws Exception {
		return new I18nDeleteRem();
	}

	@Bean
	@Lazy(true)
	public Gson getGson() {
		return new Gson();
	}

}
