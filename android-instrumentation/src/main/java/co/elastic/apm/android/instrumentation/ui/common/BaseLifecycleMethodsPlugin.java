package co.elastic.apm.android.instrumentation.ui.common;

import androidx.annotation.NonNull;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Map;

public abstract class BaseLifecycleMethodsPlugin implements Plugin {
    private Map<String, String> cachedMap = null;
    private Junction<NamedElement.WithDescriptor> cachedMatcher = null;

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder,
                                        TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        Class<?> adviceClass = getAdviceClass(getAvailableLifecycleMethodCount(typeDescription));
        if (adviceClass == null) {
            // No Operation.
            return builder;
        }
        return builder.visit(Advice.to(adviceClass).on(getMethodsMatcher()));
    }

    protected abstract Class<?> getAdviceClass(int methodCount);

    protected abstract Map<String, String> provideTargetNamesToDescriptors();

    @Override
    public void close() {
        cachedMatcher = null;
        cachedMap = null;
    }

    private Junction<NamedElement.WithDescriptor> getMethodsMatcher() {
        if (cachedMatcher == null) {
            Junction<NamedElement.WithDescriptor> elementMatcher = null;
            Map<String, String> targetNamesToDescriptors = getTargetNamesToDescriptors();
            for (String methodName : targetNamesToDescriptors.keySet()) {
                String descriptor = targetNamesToDescriptors.get(methodName);
                if (elementMatcher == null) {
                    elementMatcher = getMethodMatcher(methodName, descriptor);
                } else {
                    elementMatcher = elementMatcher.or(getMethodMatcher(methodName, descriptor));
                }
            }
            cachedMatcher = elementMatcher;
        }
        return cachedMatcher;
    }

    @NonNull
    private Junction<NamedElement.WithDescriptor> getMethodMatcher(String methodName, String descriptor) {
        return ElementMatchers.named(methodName).and(ElementMatchers.hasDescriptor(descriptor));
    }

    private int getAvailableLifecycleMethodCount(TypeDescription typeDescription) {
        int foundMethods = 0;
        Map<String, String> targetNamesToDescriptors = getTargetNamesToDescriptors();
        int maxMethods = targetNamesToDescriptors.size();
        for (MethodDescription.InDefinedShape declaredMethod : typeDescription.getDeclaredMethods()) {
            if (targetNamesToDescriptors.containsKey(declaredMethod.getName())) {
                String descriptor = targetNamesToDescriptors.get(declaredMethod.getName());
                if (declaredMethod.getDescriptor().equals(descriptor)) {
                    foundMethods++;
                }
            }
            if (foundMethods == maxMethods) {
                break;
            }
        }

        return foundMethods;
    }

    private Map<String, String> getTargetNamesToDescriptors() {
        if (cachedMap == null) {
            cachedMap = provideTargetNamesToDescriptors();
        }
        return cachedMap;
    }
}
