package com.example.jdmn.runtime;

import com.gs.dmn.runtime.ExecutionContext;
import com.gs.dmn.runtime.ExecutionContextBuilder;
import com.gs.dmn.runtime.annotation.AnnotationSet;
import com.gs.dmn.runtime.cache.DefaultCache;
import com.gs.dmn.runtime.external.DefaultExternalFunctionExecutor;
import com.gs.dmn.runtime.listener.EventListener;
import com.gs.dmn.runtime.listener.NopEventListener;

import java.util.Objects;

public final class JdmnExecutionContextFactory {
    private JdmnExecutionContextFactory() {
    }

    public static ExecutionContext standardContext() {
        return contextWithEventListener(new NopEventListener());
    }

    public static ExecutionContext contextWithEventListener(EventListener eventListener) {
        Objects.requireNonNull(eventListener, "eventListener must not be null");
        return ExecutionContextBuilder.executionContext()
                .withAnnotations(new AnnotationSet())
                .withEventListener(eventListener)
                .withExternalFunctionExecutor(new DefaultExternalFunctionExecutor())
                .withCache(new DefaultCache())
                .build();
    }
}
