package co.elastic.apm.android.instrumentation;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;

import okhttp3.EventListener;

public class OkHttpComposeFactoryPlugin implements Plugin {
    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        return builder.method(ElementMatchers.isDeclaredBy(EventListener.class))
                .intercept(Advice.to(OkHttpComposeAdvice.class));
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean matches(TypeDescription target) {
        return target.getTypeName().equals("co.elastic.apm.android.sdk.traces.okhttp.compose.CompositeEventListener");
    }
}
