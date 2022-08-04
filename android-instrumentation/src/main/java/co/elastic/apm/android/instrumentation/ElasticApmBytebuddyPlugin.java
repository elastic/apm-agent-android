package co.elastic.apm.android.instrumentation;

import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;

import java.io.IOException;

public class ElasticApmBytebuddyPlugin implements Plugin {

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder,
                                        TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        System.out.println("Type that matched: " + typeDescription.toString());//todo delete
        return builder;
    }

    @Override
    public void close() throws IOException {
        // No operation.
    }

    @Override
    public boolean matches(TypeDescription target) {
        return target.getTypeName().equals("okhttp3.OkHttpClient$Builder");
    }
}
