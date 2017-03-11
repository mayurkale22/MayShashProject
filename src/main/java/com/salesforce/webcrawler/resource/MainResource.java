package com.salesforce.webcrawler.resource;

import com.google.common.base.Strings;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/")
@SuppressWarnings("PMD.DuplicateImports")
public class MainResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(MainResource.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response showWordCloud() {
        LOGGER.info("Web Crawler server listening to GET request...");
        return Response.status(200).entity("HELLO GET!").build();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response buildCrawler(@QueryParam("url") @DefaultValue("") String url) {
        UrlValidator urlValidator = new UrlValidator();

        if (!Strings.isNullOrEmpty(url) && urlValidator.isValid(url)) {
            LOGGER.info("Web Crawler server listening to POST request...");
            return Response.status(200).entity(url).build();
        } else {
            return Response.status(400).entity("You fool! Please add url param.").build();
        }
    }
}
