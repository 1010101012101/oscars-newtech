package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.db.TagCtgRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.Tag;
import net.es.oscars.resv.ent.TagCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;


@RestController
@Slf4j
public class TagController {

    @Autowired
    private TagCtgRepository ctgRepo;
    @Autowired
    private ConnectionRepository connRepo;

    @ExceptionHandler(StartupException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public void handleStartup(StartupException ex) {
        log.warn("Still in startup");
    }

    @RequestMapping(value = "/protected/tag/categories", method = RequestMethod.GET)
    @ResponseBody
    public List<TagCategory> getCategories() {
        return ctgRepo.findAll();
    }

    @RequestMapping(value = "/protected/tag/categories/update", method = RequestMethod.POST)
    @ResponseBody
    public void update(@RequestBody TagCategory in) {
        TagCategory ctg = TagCategory.builder().category("").build();

        if (in.getId() != null) {
            Optional<TagCategory> ctgOpt = ctgRepo.findById(in.getId());
            if (!ctgOpt.isPresent()) {
                throw new NoSuchElementException("category not found");
            }
        }
        if (in.getCategory() == null || in.getCategory().length() == 0) {
            throw new IllegalArgumentException("null or empty category!");
        }
        ctg.setCategory(in.getCategory());
        ctg.setSource(in.getSource());
        ctgRepo.save(ctg);
    }
    @RequestMapping(value = "/protected/tag/categories/delete/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void delete(@PathVariable Integer id) {
        Optional<TagCategory> ctg = ctgRepo.findById(id.longValue());
        if (ctg.isPresent()) { String category = ctg.get().getCategory();
            ctgRepo.delete(ctg.get());

        }

    }
    @RequestMapping(value = "/protected/tag/categories/expunge/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void expunge(@PathVariable Integer id) {
        Optional<TagCategory> ctg = ctgRepo.findById(id.longValue());
        if (ctg.isPresent()) {
            String category = ctg.get().getCategory();
            // TODO: actually expunge
            ctgRepo.delete(ctg.get());

        }

    }

    @RequestMapping(value = "/protected/tag/delete/{connectionId:.+}/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void deleteTag(@PathVariable String connectionId, @PathVariable Long id) {
        Optional<Connection> maybeConnection = connRepo.findByConnectionId(connectionId);
        if (!maybeConnection.isPresent()) {
            throw new NoSuchElementException("connection id not found");
        } else {
            Connection c = maybeConnection.get();
            Tag deleteThis = null;
            for (Tag t : c.getTags()) {
                if (t.getId().equals(id)) {
                    deleteThis = t;
                }
            }
            if (deleteThis == null) {
                throw new NoSuchElementException("tag id not found");
            } else {
                c.getTags().remove(deleteThis);
                connRepo.save(c);
            }
        }

    }

    @RequestMapping(value = "/protected/tag/add/{connectionId:.+}", method = RequestMethod.POST)
    @ResponseBody
    public void addTag(@PathVariable String connectionId, @RequestBody Tag in) {
        if (in.getCategory() == null || in.getCategory().length() == 0) {
            throw new IllegalArgumentException("null / empty category");
        }
        if (in.getCategory() == null || in.getCategory().length() == 0) {
            throw new IllegalArgumentException("empty contents");
        }
        Tag tag = Tag.builder().category(in.getCategory()).contents(in.getContents()).build();

        Optional<Connection> maybeConnection = connRepo.findByConnectionId(connectionId);
        if (!maybeConnection.isPresent()) {
            throw new NoSuchElementException("connection id not found");
        } else {
            Connection c = maybeConnection.get();
            c.getTags().add(tag);
            connRepo.save(c);
        }

    }
}