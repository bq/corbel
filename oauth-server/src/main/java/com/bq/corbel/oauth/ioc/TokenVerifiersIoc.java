package com.bq.corbel.oauth.ioc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bq.corbel.oauth.repository.UserRepository;
import com.bq.corbel.oauth.token.verifier.UserExistsTokenVerifier;

/**
 * @author Alexander De Leon
 *
 */
@Configuration public class TokenVerifiersIoc {

    @Bean
    public UserExistsTokenVerifier userExistsTokenVerifier(UserRepository userRepository) {
        return new UserExistsTokenVerifier(userRepository);
    }

}
