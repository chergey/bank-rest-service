module cache {
    requires com.hazelcast.core;
    requires com.hazelcast.client;
    requires org.eclipse.persistence.core;
    requires org.slf4j;
    exports org.elcer.accounts.cache;
}