package net.es.oscars.pss.db;

import net.es.oscars.pss.ent.RouterCommands;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RouterCommandsRepository extends CrudRepository<RouterCommands, Long> {

    List<RouterCommands> findAll();
    List<RouterCommands> findByConnectionId(String connectionId);
    List<RouterCommands> findByConnectionIdAndDeviceUrn(String connectionId, String deviceUrn);


}