package co.elastic.apm.android.instrumentation.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.AndroidDescriptor;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;

import co.elastic.apm.android.instrumentation.ui.activities.ActivityLifecycleAdvice;

public class FragmentLifecyclePlugin implements Plugin {
    private final AndroidDescriptor androidDescriptor;

    public FragmentLifecyclePlugin(AndroidDescriptor androidDescriptor) {
        this.androidDescriptor = androidDescriptor;
    }

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder,
                                        TypeDescription typeDescription,
                                        ClassFileLocator classFileLocator) {
        return builder.visit(Advice.to(ActivityLifecycleAdvice.class).on(ElementMatchers.named("onViewCreated").and(ElementMatchers.takesArguments(View.class, Bundle.class))));
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean matches(TypeDescription target) {
        if (androidDescriptor.getTypeScope(target) == AndroidDescriptor.TypeScope.EXTERNAL) {
            return false;
        }
        return !target.getSimpleName().startsWith("Hilt_") && target.isAssignableTo(Fragment.class);
    }
}
