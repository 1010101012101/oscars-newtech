package net.es.oscars.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.resv.ent.EroHop;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PcePath {
    private double cost;

    private List<EroHop> azEro;
    private List<EroHop> zaEro;
    private Integer azAvailable;
    private Integer zaAvailable;

    private Integer azBaseline;
    private Integer zaBaseline;


}
