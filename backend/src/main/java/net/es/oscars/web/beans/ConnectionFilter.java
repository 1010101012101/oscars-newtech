package net.es.oscars.web.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties=true)
public class ConnectionFilter {

    private String connectionId;
    private String username;
    private List<Integer> vlans;
    private List<String> ports;
    private String description;
    private Phase phase;
    private State state;
    private Interval interval;
    private int page;
    private int sizePerPage;


}
