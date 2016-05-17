package com.bq.corbel.iam.eventbus;

import java.util.stream.Stream;

import com.bq.corbel.event.DeviceEvent;
import com.bq.corbel.eventbus.EventHandler;
import com.bq.corbel.iam.model.UserToken;
import com.bq.corbel.iam.repository.UserTokenRepository;
import com.bq.corbel.lib.ws.auth.repository.AuthorizationRulesRepository;

/**
 * Created by Francisco Sanchez on 18/02/16.
 */
public class DeviceDeletedEventHandler implements EventHandler<DeviceEvent> {
    private final AuthorizationRulesRepository authorizationRulesRepository;
    private final UserTokenRepository userTokenRepository;

    public DeviceDeletedEventHandler(AuthorizationRulesRepository authorizationRulesRepository, UserTokenRepository userTokenRepository) {
        this.authorizationRulesRepository = authorizationRulesRepository;
        this.userTokenRepository = userTokenRepository;
    }

    @Override
    public void handle(DeviceEvent event) {
        if (DeviceEvent.Type.DELETED.equals(event.getType())) {
            Stream<String> tokens = getAllTokensIdByDeviceId(event.getDeviceId());
            tokens.forEach(this::invalidateToken);
        }
    }

    private Stream<String> getAllTokensIdByDeviceId(String deviceId) {
        return userTokenRepository.findByDeviceId(deviceId).stream().map(UserToken::getToken);
    }

    private void invalidateToken(String accessToken) {
        authorizationRulesRepository.deleteByToken(accessToken);
        userTokenRepository.delete(accessToken);
    }

    @Override
    public Class<DeviceEvent> getEventType() {
        return DeviceEvent.class;
    }
}
