package net.es.oscars.dto.topo;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidirectionalPath {

    private List<TopoEdge> azPath;
    private List<TopoEdge> zaPath;
}
