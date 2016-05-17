package com.bq.corbel.iam.verifier;

import com.bq.corbel.iam.repository.UserRepository;
import com.bq.corbel.lib.token.exception.TokenVerificationException;
import com.bq.corbel.lib.token.reader.TokenReader;
import com.bq.corbel.lib.token.verifier.TokenVerifier;

/**
 * @author Alberto J. Rubio
 */
public class UserExistsTokenVerifier implements TokenVerifier {

    private final UserRepository userRepository;

    public UserExistsTokenVerifier(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void verify(TokenReader reader) throws TokenVerificationException {
        if (reader.getInfo().getUserId() != null && userRepository.findOne(reader.getInfo().getUserId()) == null) {
            throw new TokenVerificationException.UserNotExists();
        }
    }
}
