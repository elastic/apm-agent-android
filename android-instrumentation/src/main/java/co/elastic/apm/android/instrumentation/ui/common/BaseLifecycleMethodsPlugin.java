package co.elastic.apm.android.instrumentation.ui.common;

import androidx.annotation.NonNull;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.elastic.apm.android.sdk.internal.instrumentation.LifecycleMultiMethodSpan;

public abstract class BaseLifecycleMethodsPlugin implements Plugin {
    private Map<String, String> cachedTargetNamesToDescriptors = null;
    private Junction<NamedElement.WithDescriptor> cachedMatcher = null;

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder,
                                        TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        Junction<NamedElement.WithDescriptor> lastLifecycleMethodAvailable = getLastLifecycleMethodMatcher(typeDescription);
        if (lastLifecycleMethodAvailable == null) {
            // No Operation.
            return builder;
        }

        AnnotationDescription lastMethodAnnotation = AnnotationDescription.Builder.ofType(LifecycleMultiMethodSpan.LastMethod.class).build();
        AsmVisitorWrapper annotator = new MemberAttributeExtension.ForMethod().annotateMethod(lastMethodAnnotation).on(lastLifecycleMethodAvailable);

        return builder
                .visit(annotator)
                .visit(Advice.to(getAdviceClass()).on(getMethodsMatcher()));
    }

    @NonNull
    protected abstract Class<?> getAdviceClass();

    protected abstract Map<String, String> provideOrderedTargetNamesToDescriptors();

    @Override
    public void close() {
        cachedMatcher = null;
        cachedTargetNamesToDescriptors = null;
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

    private Junction<NamedElement.WithDescriptor> getLastLifecycleMethodMatcher(TypeDescription typeDescription) {
        int foundMethods = 0;
        Map<String, String> targetNamesToDescriptors = getTargetNamesToDescriptors();
        List<String> orderedMethodNames = new ArrayList<>(targetNamesToDescriptors.keySet());
        Map<String, String> foundMethodNamesToDescriptor = new HashMap<>();
        int maxMethods = targetNamesToDescriptors.size();
        for (MethodDescription.InDefinedShape declaredMethod : typeDescription.getDeclaredMethods()) {
            if (targetNamesToDescriptors.containsKey(declaredMethod.getName())) {
                String descriptor = targetNamesToDescriptors.get(declaredMethod.getName());
                if (declaredMethod.getDescriptor().equals(descriptor)) {
                    foundMethodNamesToDescriptor.put(declaredMethod.getName(), descriptor);
                    foundMethods++;
                }
            }
            if (foundMethods == maxMethods) {
                break;
            }
        }

        // Looping backwards to get the last lifecycle methods first.
        for (int i = orderedMethodNames.size() - 1; i >= 0; i--) {
            String methodName = orderedMethodNames.get(i);
            if (foundMethodNamesToDescriptor.containsKey(methodName)) {
                // Return the latest lifecycle method available.
                return getMethodMatcher(methodName, foundMethodNamesToDescriptor.get(methodName));
            }
        }

        // No lifecycle methods defined in the target class.
        return null;
    }

    private Map<String, String> getTargetNamesToDescriptors() {
        if (cachedTargetNamesToDescriptors == null) {
            cachedTargetNamesToDescriptors = provideOrderedTargetNamesToDescriptors();
        }
        return cachedTargetNamesToDescriptors;
    }
}
