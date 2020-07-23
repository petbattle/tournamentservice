package com.petbattle.services;


import com.petbattle.config.ProcessInfinispanAuth;
import com.petbattle.core.PetVote;
import io.quarkus.infinispan.client.Remote;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

/**
 * Service to cleanup and load application data
 */
@ApplicationScoped
public class ServiceInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInit.class.getName());
    private static final String CACHE_CONFIG_XML =
            "<infinispan><cache-container>" +
                    "<replicated-cache name=\"VotesCache\"/>" +
                    "</cache-container></infinispan>";
    @Inject
    RemoteCacheManager cacheManager;

    @ConfigProperty(name = "Infinispan.CredFileLocn", defaultValue = "test")
    String InfinispanAuthFile;

    /**
     * Listens startup event to load the data
     */
    void onStart(@Observes @Priority(value = 1) StartupEvent ev) {
        String CacheName = "VotesCache";
        LOGGER.info("Creating Tournament Cache {}",CacheName);

//        ProcessInfinispanAuth authProc = new ProcessInfinispanAuth(CacheName);
        RemoteCache x = cacheManager.administration().getOrCreateCache("VotesCache", new XMLStringConfiguration(CACHE_CONFIG_XML));
        x.addClientListener(new EventPrintListener());
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
