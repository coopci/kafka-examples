package org.apache.kafka.connect.cli;


import org.apache.kafka.common.utils.Exit;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.connect.runtime.Connect;
import org.apache.kafka.connect.runtime.ConnectorConfig;
import org.apache.kafka.connect.runtime.Herder;
import org.apache.kafka.connect.runtime.Worker;
import org.apache.kafka.connect.runtime.isolation.Plugins;
import org.apache.kafka.connect.runtime.rest.RestServer;
import org.apache.kafka.connect.runtime.rest.entities.ConnectorInfo;
import org.apache.kafka.connect.runtime.standalone.StandaloneConfig;
import org.apache.kafka.connect.runtime.standalone.StandaloneHerder;
import org.apache.kafka.connect.storage.FileOffsetBackingStore;
import org.apache.kafka.connect.util.Callback;
import org.apache.kafka.connect.util.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * Command line utility that runs Kafka Connect as a standalone process. In this mode, work is not
 * distributed. Instead, all the normal Connect machinery works within a single process. This is
 * useful for ad hoc, small, or experimental jobs.
 * </p>
 * <p>
 * By default, no job configs or offset data is persistent. You can make jobs persistent and
 * fault tolerant by overriding the settings to use file storage for both.
 * </p>
 */
public class ConnectStandalone {
    private static final Logger log = LoggerFactory.getLogger(ConnectStandalone.class);

    // run with config/connect-standalone.properties config/connect-file-source.properties
    //
    public static void main(String[] args) throws Exception {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        if (args.length < 2) {
            log.info("Usage: ConnectStandalone worker.properties connector1.properties [connector2.properties ...]");
            Exit.exit(1);
        }

        String workerPropsFile = args[0];
        Map<String, String> workerProps = !workerPropsFile.isEmpty() ?
                Utils.propsToStringMap(Utils.loadProps(workerPropsFile)) : Collections.<String, String>emptyMap();

        Time time = Time.SYSTEM;
        Plugins plugins = new Plugins(workerProps);
        plugins.compareAndSwapWithDelegatingLoader();
        StandaloneConfig config = new StandaloneConfig(workerProps);

        RestServer rest = new RestServer(config);
        URI advertisedUrl = rest.advertisedUrl();
        String workerId = advertisedUrl.getHost() + ":" + advertisedUrl.getPort();

        Worker worker = new Worker(workerId, time, plugins, config, new FileOffsetBackingStore());

        Herder herder = new StandaloneHerder(worker);
        final Connect connect = new Connect(herder, rest);

        try {
            connect.start();
            for (final String connectorPropsFile : Arrays.copyOfRange(args, 1, args.length)) {
                Map<String, String> connectorProps = Utils.propsToStringMap(Utils.loadProps(connectorPropsFile));
                FutureCallback<Herder.Created<ConnectorInfo>> cb = new FutureCallback<Herder.Created<ConnectorInfo>>(new Callback<Herder.Created<ConnectorInfo>>() {
                    @Override
                    public void onCompletion(Throwable error, Herder.Created<ConnectorInfo> info) {
                        if (error != null)
                            log.error("Failed to create job for {}", connectorPropsFile);
                        else
                            log.info("Created connector {}", info.result().name());
                    }
                });
                herder.putConnectorConfig(
                        connectorProps.get(ConnectorConfig.NAME_CONFIG),
                        connectorProps, false, cb);
                cb.get();
            }
        } catch (Throwable t) {
            log.error("Stopping after connector error", t);
            connect.stop();
        }

        // Shutdown will be triggered by Ctrl-C or via HTTP shutdown request
        connect.awaitStop();
    }
}
