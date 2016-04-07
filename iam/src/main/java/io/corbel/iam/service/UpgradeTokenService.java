package io.corbel.iam.service;

import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.lib.token.reader.TokenReader;

public interface UpgradeTokenService {

    public String[] upgradeToken(String assertion, TokenReader tokenReader) throws UnauthorizedException;
}
