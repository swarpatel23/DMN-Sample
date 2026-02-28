package com.example.jdmn.runtime;

import com.gs.dmn.runtime.ExecutionContext;
import com.gs.dmn.runtime.ExecutionContextBuilder;
import com.gs.dmn.runtime.annotation.AnnotationSet;
import com.gs.dmn.runtime.cache.DefaultCache;
import com.gs.dmn.runtime.external.DefaultExternalFunctionExecutor;
import com.gs.dmn.runtime.listener.NopEventListener;

public final class JdmnExecutionContextFactory {
    private JdmnExecutionContextFactory() {
    }

    public static ExecutionContext standardContext() {
        return ExecutionContextBuilder.executionContext()
                .withAnnotations(new AnnotationSet())
                .withEventListener(new NopEventListener())
                .withExternalFunctionExecutor(new DefaultExternalFunctionExecutor())
                .withCache(new DefaultCache())
                .build();
    }
}
