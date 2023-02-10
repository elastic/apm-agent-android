package co.elastic.apm.compile.processor.generators.instrumentations;

import javax.lang.model.element.TypeElement;

import co.elastic.apm.compile.processor.generators.Generator;

public abstract class BaseInstrumentationsGenerator implements Generator {

    protected String getInstrumentationName(TypeElement element) {
        return getInstrumentationName(element.getSimpleName().toString());
    }

    private String getInstrumentationName(String simpleName) {
        int suffixIndex = simpleName.lastIndexOf("Instrumentation");
        if (suffixIndex > 0) {
            return simpleName.substring(0, suffixIndex);
        }
        return simpleName;
    }
}
