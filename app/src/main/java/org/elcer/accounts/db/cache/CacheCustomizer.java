package org.elcer.accounts.db.cache;

import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.elcer.accounts.app.TestUtils;


public class CacheCustomizer implements DescriptorCustomizer {
    @Override
    public void customize(ClassDescriptor descriptor) {
        if (!TestUtils.TEST) {
            descriptor.setCacheInterceptorClass(AccountCacheInterceptor.class);
        }
    }
}
