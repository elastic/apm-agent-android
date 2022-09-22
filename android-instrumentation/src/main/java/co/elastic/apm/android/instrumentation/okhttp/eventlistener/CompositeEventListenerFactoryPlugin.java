package co.elastic.apm.android.instrumentation.okhttp.eventlistener;

import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

import java.io.IOException;

public class CompositeEventListenerFactoryPlugin implements Plugin {

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        return builder.visit(new CompositeEventListenerRemapper());
    }

    @Override
    public void close() throws IOException {
        // No operation.
    }

    @Override
    public boolean matches(TypeDescription target) {
        return target.getTypeName().equals("co.elastic.apm.android.common.okhttp.eventlistener.CompositeEventListenerFactory");
    }
}
