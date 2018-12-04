package net.es.oscars.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PceResponse {
    private Integer evaluated;

    private PcePath shortest;
    private PcePath leastHops;
    private PcePath fits;
    private PcePath widestSum;
    private PcePath widestAZ;
    private PcePath widestZA;

}
