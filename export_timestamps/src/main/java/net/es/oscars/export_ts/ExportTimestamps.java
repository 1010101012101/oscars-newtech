package net.es.oscars.export_ts;

import net.es.oscars.resv.db.CommandHistoryRepository;
import net.es.oscars.resv.db.HeldRepository;
import net.es.oscars.resv.db.LogRepository;
import net.es.oscars.resv.db.ScheduleRepository;
import net.es.oscars.topo.db.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExportTimestamps {
    @Autowired
    protected ScheduleRepository schRepo;
    @Autowired
    protected LogRepository logRepo;

    @Autowired
    protected HeldRepository heldRepo;

    @Autowired
    protected CommandHistoryRepository chRepo;

    @Autowired
    protected VersionRepository vRepo;

    @Transactional
    public void export() {
        Map<Long, String> lines = new HashMap<>();
        Map<Long, String> sub_lines = new HashMap<>();


        vRepo.findAll().forEach(v -> {
            Long id = v.getId();
            lines.put(id, "UPDATE version SET updated_long = "+v.getUpdated().toEpochMilli()+" where id = "+id+";");

        });
        System.out.println(String.join("\n", lines.values() ));
        lines.clear();

        chRepo.findAll().forEach(rc -> {
            Long id = rc.getId();
            lines.put(id, "UPDATE router_command_history SET date_long = "+rc.getDate().toEpochMilli()+" where id = "+id+";");

        });
        System.out.println(String.join("\n", lines.values() ));
        lines.clear();


        heldRepo.findAll().forEach(h -> {
            Long id = h.getId();
            lines.put(id, "UPDATE held SET exp_long = "+h.getExpiration().toEpochMilli()+" where id = "+id+";");

        });
        System.out.println(String.join("\n", lines.values() ));
        lines.clear();

        schRepo.findAll().forEach(s -> {
            Long id = s.getId();
            Long b = s.getBeginning().toEpochMilli();
            Long e = s.getEnding().toEpochMilli();
            lines.put(id, "UPDATE schedule SET beg_long = "+b+", end_long = "+e+" where id = "+id+";");

        });
        System.out.println(String.join("\n", lines.values() ));

        lines.clear();
        logRepo.findAll().forEach(s -> {
            Long id = s.getId();
            lines.put(id, "UPDATE event_log SET created_long = "+s.getCreated().toEpochMilli()+" where id = "+id+";");
            s.getEvents().forEach(e -> {
                Long log_id = e.getId();
                sub_lines.put(log_id, "UPDATE event SET at_long = "+e.getAt().toEpochMilli()+" where id = "+log_id+";");
            });

        });
        System.out.println(String.join("\n", lines.values() ));
        System.out.println(String.join("\n", sub_lines.values() ));
        lines.clear();
        sub_lines.clear();

    }
}
