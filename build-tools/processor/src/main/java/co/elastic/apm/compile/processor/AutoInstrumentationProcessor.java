package co.elastic.apm.compile.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
@SupportedAnnotationTypes("co.elastic.apm.compile.processor.annotations.AutoInstrumentation")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoInstrumentationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        annotations.forEach((Consumer<TypeElement>) annotation -> {
            Set<? extends Element> annotated = roundEnvironment.getElementsAnnotatedWith(annotation);
            List<TypeElement> types = new ArrayList<>();
            annotated.forEach((Consumer<Element>) element -> types.add((TypeElement) element));
            generateTypeFor(types);
        });

        return true;
    }

    private void generateTypeFor(List<TypeElement> types) {
        ClassName instrumentationClassName = ClassName.get("co.elastic.apm.android.sdk.instrumentation", "Instrumentation");
        ClassName functionClassName = ClassName.get(instrumentationClassName.packageName(), instrumentationClassName.simpleName(), "Function");
        List<MethodSpec> methods = new ArrayList<>();
        MethodSpec genericMethod = generateGenericMethod(instrumentationClassName, functionClassName);
        methods.add(genericMethod);
        types.forEach(typeElement -> methods.add(generateMethodFor(typeElement, instrumentationClassName, functionClassName)));
        generateTypeWithMethods(methods, types);
    }

    private MethodSpec generateGenericMethod(ClassName instrumentationClassName, ClassName functionClassName) {
        TypeVariableName typeVariable = TypeVariableName.get("T", instrumentationClassName);
        return MethodSpec.methodBuilder("runWhenInstrumentationIsEnabled")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(typeVariable)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), typeVariable), "type")
                .addParameter(ParameterizedTypeName.get(functionClassName, typeVariable), "function")
                .addStatement("$T.runWhenEnabled(type, function)", instrumentationClassName)
                .build();
    }

    private MethodSpec generateMethodFor(TypeElement typeElement, ClassName instrumentationClassName, ClassName functionClassName) {
        String simpleName = typeElement.getSimpleName().toString();
        String instrumentationName = getInstrumentationName(simpleName);
        ClassName typeClassName = ClassName.get(typeElement);
        ParameterSpec function = ParameterSpec.builder(ParameterizedTypeName.get(functionClassName, typeClassName), "function").build();
        return MethodSpec.methodBuilder("runWhen" + instrumentationName + "IsEnabled")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(function)
                .addStatement("$T.runWhenEnabled($T.class, $N)", instrumentationClassName, typeClassName, function)
                .build();
    }

    private void generateTypeWithMethods(List<MethodSpec> methods, List<TypeElement> from) {
        String packageName = "co.elastic.apm.android.sdk.instrumentation";
        String simpleName = "Instrumentations";
        TypeSpec typeSpec = TypeSpec.classBuilder(simpleName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethods(methods)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(packageName + "." + simpleName, from.toArray(new TypeElement[0]));
            Writer writer = sourceFile.openWriter();

            javaFile.writeTo(writer);

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getInstrumentationName(String simpleName) {
        int suffixIndex = simpleName.lastIndexOf("Instrumentation");
        if (suffixIndex > 0) {
            return simpleName.substring(0, suffixIndex);
        }
        return simpleName;
    }
}
