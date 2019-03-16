package org.elcer.accounts;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import javax.validation.constraints.NotNull;

public class RepeatableRunner extends BlockJUnit4ClassRunner {
    public RepeatableRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        if (isIgnored(method) || !isRepeatable(method)) {
            return super.describeChild(method);
        }
        return describeRepeatTest(method);
    }

    private Description describeRepeatTest(@NotNull FrameworkMethod method) {
        final int times = method.getAnnotation(Repeat.class).value();

        final var description = Description.createSuiteDescription(
                testName(method) + " [" + times + " times]",
                method.getAnnotations());

        for (int i = 1; i <= times; i++) {
            description.addChild(Description.createTestDescription(
                    getTestClass().getJavaClass(), "[" + i + "] " + testName(method)));
        }
        return description;
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        final var descriptions = describeChild(method);
        if (isIgnored(method) || !isRepeatable(method)) {
            super.runChild(method, notifier);
            return;
        }
        for (var description : descriptions.getChildren()) {
            runLeaf(methodBlock(method), description, notifier);
        }

    }

    private boolean isRepeatable(FrameworkMethod method) {
        return method.getAnnotation(Repeat.class) != null;
    }
}
