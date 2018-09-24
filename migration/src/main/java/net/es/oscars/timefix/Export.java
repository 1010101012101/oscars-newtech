package net.es.oscars.timefix;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import net.es.oscars.pss.ent.RouterCommandHistory;
import net.es.oscars.resv.db.CommandHistoryRepository;
import net.es.oscars.resv.db.LogRepository;
import net.es.oscars.resv.db.ScheduleRepository;
import net.es.oscars.resv.ent.Event;
import net.es.oscars.resv.ent.EventLog;
import net.es.oscars.resv.ent.Schedule;
import net.es.oscars.topo.db.VersionRepository;
import net.es.oscars.topo.ent.Version;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Export {
    @Autowired
    private CommandHistoryRepository historyRepo;
    @Autowired
    private VersionRepository versionRepo;

    @Autowired
    private ScheduleRepository schedRepo;

    @Autowired
    private LogRepository logRepo;


    @Transactional
    public void export() throws JsonProcessingException, IOException {

        Map<String, List<ImmutablePair<Long, Map<String, Long>>>> output = new HashMap<>();

        List<RouterCommandHistory> rch = historyRepo.findAll();
        List<ImmutablePair<Long, Map<String, Long>>> rchEntries = new ArrayList<>();

        for (RouterCommandHistory rc : rch) {
            Map<String, Long> map = new HashMap<>();
            map.put("date", rc.getDate().toEpochMilli());
            ImmutablePair<Long, Map<String, Long>> entry = new ImmutablePair<>(rc.getId(), map);
            rchEntries.add(entry);
        }
        output.put("rch", rchEntries);

        List<Version> versions = versionRepo.findAll();
        List<ImmutablePair<Long, Map<String, Long>>> vEntries = new ArrayList<>();

        for (Version v : versions) {
            Map<String, Long> map = new HashMap<>();
            map.put("updated", v.getUpdated().toEpochMilli());
            ImmutablePair<Long, Map<String, Long>> entry = new ImmutablePair<>(v.getId(), map);
            vEntries.add(entry);
        }
        output.put("versions", vEntries);


        List<Schedule> schedules = schedRepo.findAll();
        List<ImmutablePair<Long, Map<String, Long>>> schEntries = new ArrayList<>();

        for (Schedule s : schedules) {
            Map<String, Long> map = new HashMap<>();
            map.put("beginning", s.getBeginning().toEpochMilli());
            map.put("ending", s.getEnding().toEpochMilli());
            ImmutablePair<Long, Map<String, Long>> entry = new ImmutablePair<>(s.getId(), map);
            schEntries.add(entry);
        }
        output.put("schedules", schEntries);



        List<EventLog> logs = logRepo.findAll();
        List<ImmutablePair<Long, Map<String, Long>>> eventLogEntries = new ArrayList<>();
        List<ImmutablePair<Long, Map<String, Long>>> eventEntries = new ArrayList<>();

        for (EventLog el : logs) {
            Map<String, Long> elMap = new HashMap<>();
            if (el.getArchived().equals(Instant.MAX)) {

                elMap.put("archived", -1L);
            } else {
                elMap.put("archived", el.getArchived().toEpochMilli());

            }
            elMap.put("created", el.getCreated().toEpochMilli());
            ImmutablePair<Long, Map<String, Long>> elEntry = new ImmutablePair<>(el.getId(), elMap);
            eventLogEntries.add(elEntry);
            for (Event event : el.getEvents()) {
                Map<String, Long> evMap = new HashMap<>();
                evMap.put("at", event.getAt().toEpochMilli());
                ImmutablePair<Long, Map<String, Long>> evEntry = new ImmutablePair<>(event.getId(), elMap);
                eventEntries.add(evEntry);
            }
        }
        output.put("event", eventEntries);
        output.put("eventlog", eventLogEntries);




        ObjectMapper mapper = new ObjectMapper();
        //String out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(new File("timestamps.json"), output);
    }

}
