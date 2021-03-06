package net.es.oscars.topo.svc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.resv.svc.ResvLibrary;
import net.es.oscars.topo.beans.*;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.db.PortRepository;
import net.es.oscars.topo.db.VersionRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.enums.CommandParamType;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.pop.ConsistencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@Data
public class TopoService {
    private Map<String, TopoUrn> topoUrnMap;
    private List<TopoAdjcy> topoAdjcies;

    @Autowired
    private DeviceRepository deviceRepo;
    @Autowired
    private PortRepository portRepo;
    @Autowired
    private PortAdjcyRepository adjcyRepo;
    @Autowired
    private VersionRepository versionRepo;

    @Autowired
    private PssProperties pssProperties;

    // dumb cache
    private Map<String, PortBwVlan> baseline = new HashMap<>();

    @Transactional
    public void updateTopo() throws ConsistencyException, TopoException {
        Optional<Version> maybeCurrent = currentVersion();

        if (maybeCurrent.isPresent()) {
            Version current = maybeCurrent.get();
            log.info("updating pathfinding topo to version: " + current.getId());
            List<Device> devices = deviceRepo.findByVersion(current);
            List<PortAdjcy> adjcies = adjcyRepo.findByVersion(current);

            // first add all devices (and ports) to the urn map
            this.topoUrnMap = this.urnsFromDevices(devices);

            // now process all adjacencies
            this.topoAdjcies = topoAdjciesFromDevices(devices);
            this.topoAdjcies.addAll(topoAdjciesFromPortAdjcies(adjcies));
            this.baseline = new HashMap<>();


            log.info("updated with " + this.topoAdjcies.size() + " topo adjacencies");
        } else {
            throw new TopoException("no valid topology version!");
        }

    }

    public Optional<Version> currentVersion() throws ConsistencyException {
        List<Version> valid = versionRepo.findByValid(true);
        if (valid.size() == 0) {
            return Optional.empty();
        } else if (valid.size() == 1) {
            return Optional.of(valid.get(0));
        } else {
            throw new ConsistencyException("more than 1 valid versions found");
        }
    }

    public Topology currentTopology() throws ConsistencyException {
        Map<String, Device> deviceMap = new HashMap<>();
        Map<String, Port> portMap = new HashMap<>();
        List<PortAdjcy> adjcies = new ArrayList<>();
        Topology t = Topology.builder()
                .adjcies(adjcies)
                .devices(deviceMap)
                .ports(portMap)
                .build();

        if (versionRepo.findAll().size() != 0) {
            List<Version> versions = versionRepo.findByValid(true);
            if (versions.size() != 1) {
                throw new ConsistencyException("exactly one valid version can exist");
            }
            List<Device> devices = deviceRepo.findByVersion(versions.get(0));
            devices.forEach(d -> {
                deviceMap.put(d.getUrn(), d);
            });

            adjcies = adjcyRepo.findByVersion(versions.get(0));
            log.info("found " + devices.size() + " devices in version " + versions.get(0).getId());
            log.info("found " + adjcies.size() + " adjcies in version " + versions.get(0).getId());
            t.setDevices(deviceMap);
            t.setAdjcies(adjcies);

//            log.info(" current topo:");
            devices.forEach(d -> {
//                log.info(" d: "+d.getUrn());
                for (Port p : d.getPorts()) {
                    if (p.getVersion() != null && p.getVersion().getValid()) {
//                        log.info(" +- "+p.getUrn());
                        portMap.put(p.getUrn(), p);
                    }
                }
            });
        }


        return t;
    }


    public Map<String, PortBwVlan> baseline() {
        if (this.baseline.size() == 0) {
            baseline = ResvLibrary.portBwVlans(this.getTopoUrnMap(), new HashSet<>(), new HashMap<>(), new HashMap<>());
        }
        return this.baseline;
    }

    private Map<String, TopoUrn> urnsFromDevices(List<Device> devices) {
        Map<String, TopoUrn> urns = new HashMap<>();

        devices.forEach(d -> {
            if (d.getVersion().getValid()) {

                // make a copy of the IntRanges otherwise it'd be set by reference
                Set<IntRange> drv = new HashSet<>();
                drv.addAll(IntRange.mergeIntRanges(d.getReservableVlans()));
                Set<Layer> dCaps = new HashSet<>();
                dCaps.addAll(d.getCapabilities());

                TopoUrn deviceUrn = TopoUrn.builder()
                        .urn(d.getUrn())
                        .urnType(UrnType.DEVICE)
                        .device(d)
                        .reservableVlans(drv)
                        .capabilities(dCaps)
                        .reservableCommandParams(new HashSet<>())
                        .build();

                // for all devices, if this is MPLS-capable, reserve a VC id
                if (d.getCapabilities().contains(Layer.MPLS)) {
                    Set<IntRange> vcIdRanges = IntRange.fromExpression(pssProperties.getVcidRange());
                    ReservableCommandParam vcCp = ReservableCommandParam.builder()
                            .type(CommandParamType.VC_ID)
                            .reservableRanges(vcIdRanges)
                            .build();
                    deviceUrn.getReservableCommandParams().add(vcCp);
                }

                // for ALUs, add SVC, SDP and QOS ids as reservable
                if (d.getModel().equals(DeviceModel.ALCATEL_SR7750)) {

                    Set<IntRange> svcIdRanges = IntRange.fromExpression(pssProperties.getAluSvcidRange());
                    ReservableCommandParam aluSvcCp = ReservableCommandParam.builder()
                            .type(CommandParamType.ALU_SVC_ID)
                            .reservableRanges(svcIdRanges)
                            .build();
                    deviceUrn.getReservableCommandParams().add(aluSvcCp);


                    Set<IntRange> sdpIdRanges = IntRange.fromExpression(pssProperties.getAluSdpidRange());
                    ReservableCommandParam aluSdpCp = ReservableCommandParam.builder()
                            .type(CommandParamType.ALU_SDP_ID)
                            .reservableRanges(sdpIdRanges)
                            .build();
                    deviceUrn.getReservableCommandParams().add(aluSdpCp);

                    Set<IntRange> qosIdRanges = IntRange.fromExpression(pssProperties.getAluQosidRange());
                    ReservableCommandParam aluQosCp = ReservableCommandParam.builder()
                            .type(CommandParamType.ALU_QOS_POLICY_ID)
                            .reservableRanges(qosIdRanges)
                            .build();
                    deviceUrn.getReservableCommandParams().add(aluQosCp);
                }
                urns.put(d.getUrn(), deviceUrn);

                d.getPorts().forEach(p -> {
                    if (p.getVersion().getValid()) {
                        // make a copy of the IntRanges otherwise it'd be set by reference
                        Set<IntRange> prv = new HashSet<>();
                        prv.addAll(IntRange.mergeIntRanges(p.getReservableVlans()));
                        Set<Layer> pCaps = new HashSet<>();
                        pCaps.addAll(p.getCapabilities());

                        TopoUrn portUrn = TopoUrn.builder()
                                .urn(p.getUrn())
                                .urnType(UrnType.PORT)
                                .capabilities(pCaps)
                                .device(d)
                                .port(p)
                                .reservableIngressBw(p.getReservableIngressBw())
                                .reservableEgressBw(p.getReservableEgressBw())
                                .reservableVlans(prv)
                                .reservableCommandParams(new HashSet<>())
                                .build();


                        urns.put(p.getUrn(), portUrn);
                    } else {
                        log.debug("not adding invalid port " + p.getUrn());
                    }
                });
            } else {
                log.debug("not adding invalid device " + d.getUrn());

            }

        });

        return urns;

    }

    private List<TopoAdjcy> topoAdjciesFromPortAdjcies(List<PortAdjcy> portAdjcies) throws TopoException {
        List<TopoAdjcy> adjcies = new ArrayList<>();

        for (PortAdjcy pa : portAdjcies) {
            if (pa.getVersion() == null) {
                log.info("null port adjcy: " + pa.getUrn());
                continue;
            } else if (!pa.getVersion().getValid()) {
                log.info("invalid port adjcy: " + pa.getUrn());
                continue;
            }
            // all our adjcies should point to ports in the topoUrnMap

            if (!this.topoUrnMap.containsKey(pa.getA().getUrn())) {
                log.error(pa.getA().getUrn() + " -- " + pa.getZ().getUrn());

                throw new TopoException("missing A " + pa.getA().getUrn());

            } else if (!this.topoUrnMap.containsKey(pa.getZ().getUrn())) {

                log.error(pa.getA().getUrn() + " -- " + pa.getZ().getUrn());
                throw new TopoException("missing Z " + pa.getZ().getUrn());
            } else if (!pa.getA().getVersion().getValid()) {
                throw new TopoException("invalid A in adjcy" + pa.getUrn());
            } else if (!pa.getZ().getVersion().getValid()) {
                throw new TopoException("invalid Z in adjcy" + pa.getUrn());

            } else {

                TopoUrn aUrn = this.topoUrnMap.get(pa.getA().getUrn());
                TopoUrn zUrn = this.topoUrnMap.get(pa.getZ().getUrn());
                Map<Layer, Long> metrics = new HashMap<>();

                pa.getMetrics().entrySet().forEach(e -> {
                    metrics.put(e.getKey(), e.getValue());
                });

                TopoAdjcy adjcy = TopoAdjcy.builder().a(aUrn).z(zUrn).metrics(metrics).build();
                adjcies.add(adjcy);
            }

        }
        return adjcies;

    }

    private List<TopoAdjcy> topoAdjciesFromDevices(List<Device> devices) throws TopoException {
        List<TopoAdjcy> adjcies = new ArrayList<>();
        for (Device d : devices) {
            if (this.topoUrnMap.containsKey(d.getUrn())) {
                TopoUrn deviceUrn = this.topoUrnMap.get(d.getUrn());
                for (Port p : d.getPorts()) {
                    if (p.getVersion() == null || !p.getVersion().getValid()) {
                        continue;
                    }
                    if (this.topoUrnMap.containsKey(p.getUrn())) {
                        TopoUrn portUrn = this.topoUrnMap.get(p.getUrn());
                        TopoAdjcy az = TopoAdjcy.builder()
                                .a(deviceUrn)
                                .z(portUrn)
                                .metrics(new HashMap<>())
                                .build();
                        az.getMetrics().put(Layer.INTERNAL, 1L);
                        TopoAdjcy za = TopoAdjcy.builder()
                                .a(portUrn)
                                .z(deviceUrn)
                                .metrics(new HashMap<>())
                                .build();
                        za.getMetrics().put(Layer.INTERNAL, 1L);
                        adjcies.add(az);
                        adjcies.add(za);
                    } else {
                        throw new TopoException("missing a port urn "+p.getUrn());
                    }
                }
            } else {
                throw new TopoException("missing a device urn "+d.getUrn());
            }
        }

        return adjcies;
    }

}
