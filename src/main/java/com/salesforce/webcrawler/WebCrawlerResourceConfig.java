package com.salesforce.webcrawler;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class WebCrawlerResourceConfig extends ResourceConfig {

    static AbstractBinder binder = new AbstractBinder() {
        @Override
        protected void configure() {
            bind(LoadProperties.class).to(LoadProperties.class).in(Singleton.class);
        }
    };

    public static void setBinder(AbstractBinder newBinder) {
        binder = newBinder;
    }

    public WebCrawlerResourceConfig() {
        packages("com.salesforce.webcrawler.resource");
        register(binder);

        boolean withCors = true;
        //cors filter
        if (withCors) {
            register(CORSFilter.class);
        }
    }
}
