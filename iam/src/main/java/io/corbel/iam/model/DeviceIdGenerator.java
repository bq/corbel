package io.corbel.iam.model;

import io.corbel.lib.mongo.IdGenerator;
import io.corbel.lib.ws.digest.Digester;
import com.google.common.base.Joiner;

import java.util.UUID;

/**
 * @author Alexander De Leon
 * 
 */
public class DeviceIdGenerator implements IdGenerator<Device> {

    private static final String SEPARATOR = ":";
    private final Digester digester;

    public DeviceIdGenerator(Digester digester) {
        this.digester = digester;
    }

    @Override
    public String generateId(Device device) {
        return generateId(device.getDomain(), device.getUserId(), device.getUid());
    }

    public String generateId(String domain, String userId, String uid){
        return Joiner.on(SEPARATOR).join(domain, userId, uid);
    }
}
