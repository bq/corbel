package com.bq.corbel.iam.model;

import com.bq.corbel.iam.utils.UserDomainIdGenerator;
import com.bq.corbel.lib.mongo.IdGenerator;
import com.bq.corbel.lib.ws.digest.Digester;
import com.google.common.base.Joiner;

import java.util.UUID;

/**
 * @author Alexander De Leon
 * 
 */
public class DeviceIdGenerator implements IdGenerator<Device> {


    public DeviceIdGenerator() {
    }

    @Override
    public String generateId(Device device) {
        return UserDomainIdGenerator.generateDeviceId(device.getDomain(), device.getUserId(), device.getUid());
    }
}
