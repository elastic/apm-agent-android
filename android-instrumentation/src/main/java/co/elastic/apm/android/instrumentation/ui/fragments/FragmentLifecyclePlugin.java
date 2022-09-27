package co.elastic.apm.android.instrumentation.ui.fragments;

import androidx.fragment.app.Fragment;

import net.bytebuddy.build.AndroidDescriptor;
import net.bytebuddy.description.type.TypeDescription;

import java.util.HashMap;
import java.util.Map;

import co.elastic.apm.android.instrumentation.ui.common.BaseLifecycleMethodsPlugin;

public class FragmentLifecyclePlugin extends BaseLifecycleMethodsPlugin {
    private final AndroidDescriptor androidDescriptor;

    public FragmentLifecyclePlugin(AndroidDescriptor androidDescriptor) {
        this.androidDescriptor = androidDescriptor;
    }

    @Override
    protected Class<?> getAdviceClass(int methodCount) {
        switch (methodCount) {
            case 2:
                return Fragment2LifecycleMethodAdvice.class;
            case 3:
                return Fragment3LifecycleMethodAdvice.class;
            default:
                return null;
        }
    }

    @Override
    protected Map<String, String> provideTargetNamesToDescriptors() {
        Map<String, String> targets = new HashMap<>();
        targets.put("onCreate", "(Landroid/os/Bundle;)V");
        targets.put("onCreateView", "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;");
        targets.put("onViewCreated", "(Landroid/view/View;Landroid/os/Bundle;)V");
        return targets;
    }

    @Override
    public boolean matches(TypeDescription target) {
        if (androidDescriptor.getTypeScope(target) == AndroidDescriptor.TypeScope.EXTERNAL) {
            return false;
        }
        return !target.getSimpleName().startsWith("Hilt_") && target.isAssignableTo(Fragment.class);
    }
}
