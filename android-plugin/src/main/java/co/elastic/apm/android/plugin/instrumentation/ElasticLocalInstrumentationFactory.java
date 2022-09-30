package co.elastic.apm.android.plugin.instrumentation;

import com.android.build.api.instrumentation.AsmClassVisitorFactory;
import com.android.build.api.instrumentation.ClassContext;
import com.android.build.api.instrumentation.ClassData;
import com.android.build.api.instrumentation.InstrumentationParameters;

import org.objectweb.asm.ClassVisitor;

import co.elastic.apm.android.plugin.instrumentation.remapping.CoroutineBuilderRemapper;

public abstract class ElasticLocalInstrumentationFactory implements AsmClassVisitorFactory<InstrumentationParameters.None> {

    @Override
    public ClassVisitor createClassVisitor(ClassContext classContext, ClassVisitor classVisitor) {
        return new CoroutineBuilderRemapper(classVisitor);
    }

    @Override
    public boolean isInstrumentable(ClassData classData) {
        return true;
    }
}
