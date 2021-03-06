package net.es.oscars.topo.ent;

import lombok.*;
import net.es.oscars.topo.enums.Layer;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EqualsAndHashCode(exclude={"a", "z"})
public class PortAdjcy {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Port a;

    @ManyToOne
    private Port z;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable
    private Map<Layer, Long> metrics = new HashMap<>();

    @ManyToOne
    private Version version;

    public String getUrn() {
        return a.getUrn()+" - "+z.getUrn();
    }

    public Integer minimalReservableBandwidth() {
        Set<Integer> reservableBandwidths = new HashSet<>();
        reservableBandwidths.add(this.a.getReservableEgressBw());
        reservableBandwidths.add(this.z.getReservableEgressBw());
        reservableBandwidths.add(this.a.getReservableIngressBw());
        reservableBandwidths.add(this.z.getReservableIngressBw());
        // we can get() because the stream is not empty
        return reservableBandwidths.stream().min(Integer::compare).get();
    }
    public String toString() {
        return this.getClass().getSimpleName() + "-" +this.getUrn()+" "+ getId();
    }


}
