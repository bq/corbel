package com.bq.corbel.iam.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.bq.corbel.iam.model.Device;
import com.bq.corbel.lib.mongo.repository.PartialUpdateRepository;
import com.bq.corbel.lib.queries.mongo.repository.GenericFindRepository;

/**
 * @author Francisco Sanchez
 */
public interface DeviceRepository
 extends CrudRepository<Device, String>, PartialUpdateRepository<Device, String>,
        GenericFindRepository<Device, String>, DeviceRepositoryCustom {

    List<Device> findByUserId(String userId);

    Device findById(String id);

    Long deleteById(String id);

    List<Device> deleteByUserId(String id);

}
