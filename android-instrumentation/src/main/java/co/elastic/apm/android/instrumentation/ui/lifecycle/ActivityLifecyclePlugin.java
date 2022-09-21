package co.elastic.apm.android.instrumentation.ui.lifecycle;

import android.os.Bundle;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;

public class ActivityLifecyclePlugin implements Plugin {

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder,
                                        TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        return builder.visit(Advice.to(ActivityLifecycleAdvice.class).on(ElementMatchers.named("onCreate").and(ElementMatchers.takesArguments(Bundle.class))));
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean matches(TypeDescription target) {
//        if (target.getPackage() != null && !target.getPackage().getActualName().startsWith("co.elastic.apm.opbeans")) {//todo get package name as arg
//            return false;
//        }
//        return !target.getSimpleName().startsWith("Hilt_") && target.isAssignableTo(Activity.class);
        return target.getTypeName().equals("co.elastic.apm.opbeans.HomeActivity");
    }
}
