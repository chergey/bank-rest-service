package org.elcer.accounts.hk2;

import org.glassfish.hk2.api.Injectee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

enum Beans {
    LOGGER(Logger.class) {
        @Override
        @SuppressWarnings("unchecked")
        <T> T create(Injectee injectee) {
            return (T) LoggerFactory.getLogger(injectee.getInjecteeClass());
        }
    };

    Beans(Class<?> clazz) {
        this.clazz = clazz;
    }

    abstract <T> T create(Injectee injectee);

    protected Class<?> clazz;

}
