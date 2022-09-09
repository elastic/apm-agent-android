package co.elastic.apm.android.instrumentation.okhttp.eventlistener;

import static net.bytebuddy.matcher.ElementMatchers.not;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;

import okhttp3.EventListener;

public class CompositeEventListenerPlugin implements Plugin {
    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        return builder.method(ElementMatchers.isDeclaredBy(EventListener.class)
                .and(not(ElementMatchers.isStatic()))
                .and(not(ElementMatchers.isConstructor()))
        ).intercept(Advice.to(CompositeEventListenerAdvice.class));
    }

    @Override
    public void close() throws IOException {
        // No operation.
    }

    @Override
    public boolean matches(TypeDescription target) {
        return target.getTypeName().equals("co.elastic.apm.android.sdk.traces.okhttp.compose.CompositeEventListener");
    }
}
