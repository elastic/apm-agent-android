package co.elastic.apm.android.instrumentation.generic;

import net.bytebuddy.build.AndroidDescriptor;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

public class LocalRemapperPlugin implements Plugin {
    private final AndroidDescriptor androidDescriptor;

    public LocalRemapperPlugin(AndroidDescriptor androidDescriptor) {
        this.androidDescriptor = androidDescriptor;
    }

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        return builder.visit(new LocalClassesRemapper());
    }

    @Override
    public void close() {

    }

    @Override
    public boolean matches(TypeDescription typeDefinitions) {
        return androidDescriptor.getTypeScope(typeDefinitions) == AndroidDescriptor.TypeScope.LOCAL;
    }
}
