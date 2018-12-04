package net.es.oscars.pss.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.*;
import net.es.oscars.rest.RestConfigurer;
import net.es.oscars.rest.RestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;


@Component
@Slf4j
public class BackendServer implements BackendProxy {

    private String backendUrl;

    private RestTemplate restTemplate;


    @Autowired
    public BackendServer(@Value("${backend.url}") String backendUrl, RestProperties restProperties, RestConfigurer restConfigurer) {

        try {
            this.restTemplate = new RestTemplate(restConfigurer.getRestConfig(restProperties));

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        this.backendUrl = backendUrl;
        log.info("backend server URL: " + this.backendUrl);
    }

    public GeneratedCommands commands(String connectionId, String device) {
        String submitUrl = "/api/pss/generated/" + connectionId + "/" + device;
        String restPath = backendUrl + submitUrl;
        log.info("calling " + restPath);
        return restTemplate.getForObject(restPath, GeneratedCommands.class);

    }


}