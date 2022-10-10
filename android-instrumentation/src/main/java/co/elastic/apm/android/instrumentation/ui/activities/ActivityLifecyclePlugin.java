package co.elastic.apm.android.instrumentation.ui.activities;

import android.app.Activity;

import androidx.annotation.NonNull;

import net.bytebuddy.build.AndroidDescriptor;
import net.bytebuddy.description.type.TypeDescription;

import java.util.HashMap;
import java.util.Map;

import co.elastic.apm.android.instrumentation.ui.common.BaseLifecycleMethodsPlugin;

public class ActivityLifecyclePlugin extends BaseLifecycleMethodsPlugin {
    private final AndroidDescriptor androidDescriptor;

    public ActivityLifecyclePlugin(AndroidDescriptor androidDescriptor) {
        this.androidDescriptor = androidDescriptor;
    }

    @Override
    public boolean matches(TypeDescription target) {
        if (androidDescriptor.getTypeScope(target) == AndroidDescriptor.TypeScope.EXTERNAL) {
            return false;
        }
        return !target.getSimpleName().startsWith("Hilt_") && target.isAssignableTo(Activity.class);
    }

    @NonNull
    @Override
    protected Class<?> getAdviceClass() {
        return ActivityLifecycleMethodAdvice.class;
    }

    @Override
    protected Map<String, String> provideOrderedTargetNamesToDescriptors() {
        Map<String, String> targets = new HashMap<>();
        targets.put("onCreate", "(Landroid/os/Bundle;)V");
        targets.put("onStart", "()V");
        targets.put("onResume", "()V");
        return targets;
    }
}
