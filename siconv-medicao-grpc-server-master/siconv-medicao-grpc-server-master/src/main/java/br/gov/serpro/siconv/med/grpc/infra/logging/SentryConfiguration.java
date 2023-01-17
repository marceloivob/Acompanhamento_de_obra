package br.gov.serpro.siconv.med.grpc.infra.logging;

import static io.sentry.DefaultSentryClientFactory.IN_APP_FRAMES_OPTION;

import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogManager;

import javax.enterprise.event.Observes;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.jul.SentryHandler;

public class SentryConfiguration {

    @ConfigProperty(name = "sentry")
    boolean enable;

    @ConfigProperty(name = "sentry.dsn")
    public Optional<String> dsn;

    @ConfigProperty(name = "sentry.environment")
    public Optional<String> environment;

    @ConfigProperty(name = "sentry.level", defaultValue = "WARN")
    public Level level;

    @ConfigProperty(name = "sentry.in-app-packages")
    public Optional<String> inAppPackages;

    public void onStart(@Observes StartupEvent ev) throws URISyntaxException {

        if (enable) {

            if (!dsn.isPresent()) {
                throw new ConfigurationException(
                        "Configuration key \"sentry.dsn\" is required when Sentry is enabled, but its value is empty/missing");
            }

            URIBuilder uriDsn = new URIBuilder(dsn.get());

            inAppPackages.ifPresent(app -> uriDsn.addParameter(IN_APP_FRAMES_OPTION, app));

            SentryClient client = Sentry.init(uriDsn.toString());

            environment.ifPresent(client::setEnvironment);

            SentryHandler handler = new SentryHandler();
            handler.setPrintfStyle(true);
            handler.setLevel(level);

            LogManager.getLogManager().getLogger("").addHandler(handler);
        }
    }
}
