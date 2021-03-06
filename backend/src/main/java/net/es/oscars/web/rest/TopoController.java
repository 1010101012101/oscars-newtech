package net.es.oscars.web.rest;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.resv.svc.ResvLibrary;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.beans.*;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.pop.ConsistencyException;
import net.es.oscars.topo.svc.ConsistencyService;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.Interval;
import net.es.oscars.web.beans.SimpleAdjcy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
public class TopoController {

    @Autowired
    private TopoService topoService;

    @Autowired
    private ConsistencyService consistencySvc;

    @Autowired
    private ResvService resvService;

    @Autowired
    private Startup startup;

    // cache these in memory
    private Map<String, List<Port>> eppd = new HashMap<>();
    private Map<String, PortBwVlan> baseline = new HashMap<>();

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }

    @ExceptionHandler(StartupException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public void handleStartup(StartupException ex) {
        log.warn("Still in startup");
    }

    @RequestMapping(value = "/api/topo/ethernetPortsByDevice", method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public Map<String, List<Port>> ethernetPortsByDevice()
            throws ConsistencyException, StartupException {

        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (eppd.size() == 0) {
            Topology topology = topoService.currentTopology();

            for (Device d : topology.getDevices().values()) {
                List<Port> ports = new ArrayList<>();
                for (Port p: d.getPorts()) {
                    if (p.getCapabilities().contains(Layer.ETHERNET)
                            && p.getVersion() != null && p.getVersion().getValid()) {
                        ports.add(p);
                    }
                }
                eppd.put(d.getUrn(), ports);

            }
        }

        return eppd;
    }

    @RequestMapping(value = "/api/topo/adjacencies", method = RequestMethod.GET)
    @ResponseBody
    public Set<SimpleAdjcy> adjacencies()
            throws StartupException {

        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        List<TopoAdjcy> topoAdjcies = topoService.getTopoAdjcies();


        Set<SimpleAdjcy> simpleAdjcies = new HashSet<>();
        for (TopoAdjcy adjcy : topoAdjcies) {
            if (adjcy.getA().getUrnType().equals(UrnType.PORT) &&
                adjcy.getZ().getUrnType().equals(UrnType.PORT)) {
                SimpleAdjcy simpleAdjcy = SimpleAdjcy.builder()
                        .a(adjcy.getA().getDevice().getUrn())
                        .b(adjcy.getA().getPort().getUrn())
                        .y(adjcy.getZ().getPort().getUrn())
                        .z(adjcy.getZ().getDevice().getUrn())
                        .build();
                simpleAdjcies.add(simpleAdjcy);
            }
        }

        return simpleAdjcies;
    }


    @RequestMapping(value = "/api/topo/baseline", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, PortBwVlan> baseline() throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        return topoService.baseline();

    }


    @RequestMapping(value = "/api/topo/available", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, PortBwVlan> available(@RequestBody Interval interval) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }


        return resvService.available(interval, null);

    }

    @RequestMapping(value = "/api/topo/version", method = RequestMethod.GET)
    @ResponseBody
    public Version version() throws StartupException, ConsistencyException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        return topoService.currentVersion().orElseThrow(NoSuchElementException::new);


    }

    @RequestMapping(value = "/api/topo/report", method = RequestMethod.GET)
    @ResponseBody
    public ConsistencyReport report() throws StartupException  {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        return consistencySvc.getLatestReport();


    }

}