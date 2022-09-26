package co.elastic.apm.android.instrumentation.ui.activities;

import android.app.Activity;
import android.os.Bundle;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.AndroidDescriptor;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ActivityLifecyclePlugin implements Plugin {
    private final AndroidDescriptor androidDescriptor;
    private final Map<String, String> targetMethodNamesToDescriptors = new HashMap<>();

    public ActivityLifecyclePlugin(AndroidDescriptor androidDescriptor) {
        this.androidDescriptor = androidDescriptor;
        targetMethodNamesToDescriptors.put("onCreate", "(Landroid/os/Bundle;)V");
        targetMethodNamesToDescriptors.put("onStart", "()V");
        targetMethodNamesToDescriptors.put("onResume", "()V");
    }

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder,
                                        TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        Class<?> adviceClass = getAdviceClass(typeDescription);
        if (adviceClass == null) {
            // No Operation.
            return builder;
        }
        return builder.visit(Advice.to(adviceClass)
                .on(ElementMatchers.named("onCreate").and(ElementMatchers.takesArguments(Bundle.class))
                        .or(ElementMatchers.named("onResume").and(ElementMatchers.takesNoArguments()))
                        .or(ElementMatchers.named("onStart").and(ElementMatchers.takesNoArguments()))
                )
        );
    }

    private Class<?> getAdviceClass(TypeDescription typeDescription) {
        int lifecycleMethodCount = getLifecycleMethodCount(typeDescription);

        switch (lifecycleMethodCount) {
            case 2:
                return Activity2LifecycleMethodsAdvice.class;
            case 3:
                return Activity3LifecycleMethodsAdvice.class;
            default:
                return null;
        }
    }

    private int getLifecycleMethodCount(TypeDescription typeDescription) {
        int foundMethods = 0;
        for (MethodDescription.InDefinedShape declaredMethod : typeDescription.getDeclaredMethods()) {
            if (targetMethodNamesToDescriptors.containsKey(declaredMethod.getName())) {
                String descriptor = targetMethodNamesToDescriptors.get(declaredMethod.getName());
                if (declaredMethod.getDescriptor().equals(descriptor)) {
                    foundMethods++;
                }
            }
            if (foundMethods == 3) {
                break;
            }
        }

        return foundMethods;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean matches(TypeDescription target) {
        if (androidDescriptor.getTypeScope(target) == AndroidDescriptor.TypeScope.EXTERNAL) {
            return false;
        }
        return !target.getSimpleName().startsWith("Hilt_") && target.isAssignableTo(Activity.class);
    }
}
