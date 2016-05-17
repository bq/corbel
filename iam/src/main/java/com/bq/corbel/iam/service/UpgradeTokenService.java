package com.bq.corbel.iam.service;

import com.bq.corbel.iam.exception.UnauthorizedException;
import com.bq.corbel.lib.token.reader.TokenReader;

import java.util.Set;

public interface UpgradeTokenService {

    Set<String> getScopesFromTokenToUpgrade(String assertion) throws UnauthorizedException;

    void upgradeToken(String assertion, TokenReader tokenReader, Set<String> scopes) throws UnauthorizedException;
}
