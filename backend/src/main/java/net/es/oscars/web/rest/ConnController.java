package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PCEException;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.pss.ent.RouterCommandHistory;
import net.es.oscars.resv.db.CommandHistoryRepository;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.enums.BuildMode;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.web.beans.ConnChangeResult;
import net.es.oscars.web.beans.ConnException;
import net.es.oscars.web.beans.ConnectionFilter;
import net.es.oscars.web.beans.ConnectionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@Slf4j
public class ConnController {
    @Autowired
    private Startup startup;

    @Autowired
    private ConnectionRepository connRepo;
    @Autowired
    private CommandHistoryRepository historyRepo;

    @Autowired
    private ConnService connSvc;

    @ExceptionHandler(StartupException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public void handleStartup(StartupException ex) {
        log.warn("Still in startup");
    }

    @ExceptionHandler(ConnException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public void handleMiscException(ConnException ex) {
        log.warn("conn request error", ex);
    }
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist");
    }



    @RequestMapping(value = "/protected/conn/generateId", method = RequestMethod.GET)
    public String generateConnectionId() throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }


        return connSvc.generateConnectionId();


    }


    @RequestMapping(value = "/protected/conn/commit", method = RequestMethod.POST)
    @ResponseBody
    public ConnChangeResult commit(Authentication authentication, @RequestBody String connectionId)
            throws StartupException, PSSException, PCEException, ConnException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (connectionId == null || connectionId.equals("")) {
            throw new ConnException("empty or null connectionid!");
        }
        log.info("committing " + connectionId);

        String username = authentication.getName();
        Connection c;

        Optional<Connection> d = connRepo.findByConnectionId(connectionId);
        if (!d.isPresent()) {
            throw new ConnException("connection not found for id " + connectionId);

        } else {
            log.info("found connection from connectionId...");
            c = d.get();
        }
        // String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(c);
        // log.debug("committing conn: \n"+pretty);

        return connSvc.commit(c);
    }

    @RequestMapping(value = "/protected/conn/uncommit", method = RequestMethod.POST)
    @ResponseBody
    public ConnChangeResult uncommit(@RequestBody String connectionId) throws StartupException, ConnException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (connectionId == null || connectionId.equals("")) {
            throw new ConnException("empty or null connectionid!");
        }

        Optional<Connection> d = connRepo.findByConnectionId(connectionId);
        if (!d.isPresent()) {
            throw new ConnException("connection not found for id " + connectionId);
        } else {
            return connSvc.uncommit(d.get());
        }
    }


    @RequestMapping(value = "/protected/conn/release", method = RequestMethod.POST)
    @ResponseBody
    public ConnChangeResult release(@RequestBody String connectionId) throws StartupException, ConnException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (connectionId == null || connectionId.equals("")) {
            throw new ConnException("empty or null connectionid!");
        }

        Optional<Connection> c = connRepo.findByConnectionId(connectionId);
        if (!c.isPresent()) {
            throw new ConnException("connection not found for id " + connectionId);
        } else if (c.get().getPhase().equals(Phase.ARCHIVED)) {
            throw new ConnException("Cannot cancel ARCHIVED connection");
        } else {
            return connSvc.release(c.get());
        }
    }

    @RequestMapping(value = "/protected/conn/mode/{connectionId:.+}", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public Connection setMode(@PathVariable String connectionId, @RequestBody String mode)
            throws StartupException, ConnException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        Optional<Connection> cOpt = connRepo.findByConnectionId(connectionId);
        if (!cOpt.isPresent()) {
            throw new ConnException("connection not found for id " + connectionId);
        } else {
            Connection c = cOpt.get();
            if (!c.getPhase().equals(Phase.RESERVED)) {
                throw new ConnException("invalid phase: " + c.getPhase() + " for connection " + connectionId);
            }
            log.info(c.getConnectionId() + " setting build mode to " + mode);
            c.setMode(BuildMode.valueOf(mode));
            connRepo.save(c);
            return c;
        }

    }


    @RequestMapping(value = "/protected/conn/state/{connectionId:.+}", method = RequestMethod.POST)
    @Transactional
    public void setState(@PathVariable String connectionId, @RequestBody String state)
            throws StartupException, ConnException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }
        Optional<Connection> cOpt = connRepo.findByConnectionId(connectionId);
        if (!cOpt.isPresent()) {
            throw new ConnException("connection not found for id " + connectionId);
        } else {
            Connection c = cOpt.get();
            log.info(c.getConnectionId() + " overriding state to " + state);
            c.setState(State.valueOf(state));
            connRepo.save(c);
        }

    }

    @RequestMapping(value = "/api/conn/info/{connectionId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public Connection info(@PathVariable String connectionId) throws StartupException, NoSuchElementException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (connectionId == null || connectionId.equals("")) {
            log.info("no connectionId!");
            return null;
        }
//        log.info("looking for connectionId "+ connectionId);
        Optional<Connection> cOpt = connRepo.findByConnectionId(connectionId);
        if (cOpt.isPresent()) {
            return cOpt.get();
        } else {
            throw new NoSuchElementException("connection not found for id " + connectionId);

        }
    }

    @RequestMapping(value = "/api/conn/history/{connectionId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public List<RouterCommandHistory> history(@PathVariable String connectionId) throws StartupException, ConnException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        if (connectionId == null || connectionId.equals("")) {
            log.info("no connectionId!");
            throw new ConnException("no connectionId");
        }
//        log.info("looking for connectionId "+ connectionId);
        return historyRepo.findByConnectionId(connectionId);
    }

    @RequestMapping(value = "/api/conn/list", method = RequestMethod.POST)
    @ResponseBody
    public ConnectionList list(@RequestBody ConnectionFilter filter) throws StartupException {
        if (startup.isInStartup()) {
            throw new StartupException("OSCARS starting up");
        } else if (startup.isInShutdown()) {
            throw new StartupException("OSCARS shutting down");
        }

        return connSvc.filter(filter);
    }


}