package org.elcer.accounts.cache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;

import java.util.Collections;


public class HazelcastCacheCreator implements CacheCreator {

    private final HazelcastInstance hzClient;

    public HazelcastCacheCreator(String url) {
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
        clientNetworkConfig.setAddresses(Collections.singletonList(url));

        ClientConfig config = new ClientConfig();
        config.setNetworkConfig(clientNetworkConfig);

        hzClient = HazelcastClient.newHazelcastClient(config);
        initializeCache(hzClient.getConfig());

    }

    private void initializeCache(Config config) {
        config.addMapConfig(getMapConfig(300).setName("accounts"));
    }

    private MapConfig getMapConfig(int ttl) {
        return new MapConfig().setTimeToLiveSeconds(ttl).setEvictionPolicy(EvictionPolicy.LFU);
    }

    public <K, V> Cache<K, V> getOrCreateCache(String name) {
        return new HazelcastCache<>(hzClient.getMap(name));
    }
}
