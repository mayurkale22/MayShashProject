package com.salesforce.webcrawler.resource;

import com.google.common.base.Strings;
import com.salesforce.kspout.WordKafkaUtil;
import com.salesforce.webcrawler.LoadProperties;

import orestes.bloomfilter.BloomFilter;
import orestes.bloomfilter.FilterBuilder;

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;


@Path("/")
@SuppressWarnings("PMD.DuplicateImports")
public class MainResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(MainResource.class);
    private static final String URL_BLOOM_FILTER = "urlbloomfilter";

    @Inject LoadProperties properties;
    Producer producer = WordKafkaUtil.createProducer();

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
            LOGGER.info("Starting Web Crawler request: {}",url);

            //Open a Redis-backed Bloom filter
            BloomFilter<String> urlFilter = new FilterBuilder(10000000, 0.01)
                    .name(URL_BLOOM_FILTER)
                    .redisBacked(true)
                    .redisHost(properties.getRedisHost())
                    .redisPort(properties.getRedisPort())
                    .buildBloomFilter();

            if (urlFilter.contains(url)) {
                return Response.status(400).entity("You failed to remember the already crawled url!").build();
            }

            urlFilter.add(url);
            producer.send(new KeyedMessage<String, String>(WordKafkaUtil.topic, url));
            return Response.status(200).entity(url).build();
        } else {
            return Response.status(400).entity("You fool! Please add url param.").build();
        }
    }
}
