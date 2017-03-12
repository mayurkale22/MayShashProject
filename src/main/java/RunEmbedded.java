import com.salesforce.webcrawler.LoadProperties;
import com.salesforce.webcrawler.WebCrawlerResourceConfig;
import com.sun.net.httpserver.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RunEmbedded {

    public static void main(String[] args) {
        HttpServer server = null;
        try {

            AbstractBinder developmentBinder = new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(LoadProperties.class).to(LoadProperties.class).in(Singleton.class);
                }
            };

            server = startLemInJdkServer(developmentBinder, null, null);

            System.out.println("Hit any key to stop");
            System.in.read();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        } finally {
            System.out.println("Stopping server...");
            if (server != null) {
                server.stop(0);
            }
        }
    }

    public static HttpServer startLemInJdkServer(@Nullable AbstractBinder binder, @Nullable Integer port, @Nullable Boolean withMonitor) {

        if (port == null) {
            port = 8000;
        }

        URI baseUri = UriBuilder.fromUri("http://localhost/").port(port).build();
        if (binder != null) {
            WebCrawlerResourceConfig.setBinder(binder);
        }

        System.out.println("Starting server, " + baseUri);
        System.out.println("Application wadl in: " + baseUri + "application.wadl");
        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, new WebCrawlerResourceConfig());
        return server;
    }
}
