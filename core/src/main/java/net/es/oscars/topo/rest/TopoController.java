package net.es.oscars.topo.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.topo.ent.*;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class TopoController {
    @Autowired
    private TopoService svc;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn(ex.getMessage(), ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn(ex.getMessage(), ex);
    }


    @RequestMapping(value = "/device/{urn}", method = RequestMethod.GET)
    @ResponseBody
    public EDevice device(@PathVariable("urn") String urn) {
        return svc.device(urn);
    }

    @RequestMapping(value = "/topo/layer/{layer}", method = RequestMethod.GET)
    @ResponseBody
    public Topology layer(@PathVariable("layer") String layer) {
        return svc.layer(layer);
    }

    @RequestMapping(value = "/constraining", method = RequestMethod.GET)
    @ResponseBody
    public List<TopoResource> constraining() {
        return svc.constraining();
    }

    @RequestMapping(value = "/topo/vlanEdges", method = RequestMethod.GET)
    @ResponseBody
    public List<String> vlanEdges() {
        log.info("getting vlan edges");
        return svc.edges(Layer.ETHERNET);
    }


}