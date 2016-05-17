package com.bq.corbel.iam.auth.provider;

import com.bq.corbel.iam.model.Domain;

/**
 * @author Rubén Carrasco
 *
 */
public interface AuthorizationProviderFactory {

    Provider getProvider(Domain domain, String oAuthService);

}