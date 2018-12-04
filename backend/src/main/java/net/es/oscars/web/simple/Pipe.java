package net.es.oscars.web.simple;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.web.dto.PceMode;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pipe {
    protected String a;
    protected String z;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Integer mbps;

    protected Integer azMbps;
    protected Integer zaMbps;
    protected Boolean protect;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected PceMode pceMode;

    protected List<String> ero = new ArrayList<>();
    protected List<String> exclude = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Validity validity;

}
