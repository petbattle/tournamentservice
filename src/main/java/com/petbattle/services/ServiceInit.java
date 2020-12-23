package com.petbattle.services;


import io.quarkus.deployment.util.FileUtil;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.util.ClassPathUtils;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryRemoved;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryRemovedEvent;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Service to cleanup and load application data
 */
@ApplicationScoped
public class ServiceInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInit.class.getName());
    private static final String VOTES_CACHE_CONFIG_XML =
            "<infinispan><cache-container>" +
                    "<replicated-cache name=\"VotesCache\"/>" +
                    "</cache-container></infinispan>";

    private static final String ACTIVETOUR_CACHE_CONFIG_XML =
            "<infinispan><cache-container>" +
                    "<replicated-cache name=\"ActiveTournament\"/>" +
                    "</cache-container></infinispan>";

    @Inject
    RemoteCacheManager cacheManager;

    /**
     * Listens startup event to load the data
     */
    void onStart(@Observes @Priority(value = 1) StartupEvent ev) {
        LOGGER.info("Creating Caches VotesCache & ActiveTournament");
        //TODO : Need to add auth pulled from secret

        RemoteCache y = cacheManager.administration().getOrCreateCache("ActiveTournament", new XMLStringConfiguration(ACTIVETOUR_CACHE_CONFIG_XML));
        RemoteCache x = cacheManager.administration().getOrCreateCache("VotesCache", new XMLStringConfiguration(VOTES_CACHE_CONFIG_XML));
    }


    @ClientListener
    static class EventPrintListener {

        @ClientCacheEntryCreated
        public void handleCreatedEvent(ClientCacheEntryCreatedEvent e) {
            LOGGER.info("Someone has created an entry: " + e);
        }

        @ClientCacheEntryModified
        public void handleModifiedEvent(ClientCacheEntryModifiedEvent e) {
            LOGGER.info("Someone has modified an entry: " + e);
        }

        @ClientCacheEntryRemoved
        public void handleRemovedEvent(ClientCacheEntryRemovedEvent e) {
            LOGGER.info("Someone has removed an entry: " + e);
        }
    }
}
