package co.elastic.apm.compile.processor.generators.instrumentations;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class InstrumentationsGenerator extends BaseInstrumentationsGenerator {

    @Override
    public Result generate(List<TypeElement> from) {
        return generateTypeFor(from);
    }

    private Result generateTypeFor(List<TypeElement> types) {
        ClassName instrumentationClassName = ClassName.get("co.elastic.apm.android.sdk.instrumentation", "Instrumentation");
        ClassName functionClassName = ClassName.get(instrumentationClassName.packageName(), instrumentationClassName.simpleName(), "Function");
        List<MethodSpec> methods = new ArrayList<>();
        MethodSpec genericRunWhenEnabledMethod = generateGenericRunWhenEnabledMethod(instrumentationClassName, functionClassName);
        methods.add(genericRunWhenEnabledMethod);
        types.forEach(typeElement -> {
            String instrumentationName = getInstrumentationName(typeElement);
            ClassName typeClassName = ClassName.get(typeElement);
            methods.add(generateRunWhenEnabledFor(instrumentationName, typeClassName, instrumentationClassName, functionClassName));
            methods.add(generateIsEnabledFor(instrumentationName, typeClassName, instrumentationClassName));
        });
        return generateTypeWithMethods(instrumentationClassName.packageName(), methods, types);
    }

    private MethodSpec generateGenericRunWhenEnabledMethod(ClassName instrumentationClassName, ClassName functionClassName) {
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

    private MethodSpec generateRunWhenEnabledFor(String instrumentationName, ClassName typeClassName, ClassName instrumentationClassName, ClassName functionClassName) {
        ParameterSpec function = ParameterSpec.builder(ParameterizedTypeName.get(functionClassName, typeClassName), "function").build();
        return MethodSpec.methodBuilder("runWhen" + instrumentationName + "IsEnabled")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(function)
                .addStatement("$T.runWhenEnabled($T.class, $N)", instrumentationClassName, typeClassName, function)
                .build();
    }

    private MethodSpec generateIsEnabledFor(String instrumentationName, ClassName typeClassName, ClassName instrumentationClassName) {
        return MethodSpec.methodBuilder("is" + instrumentationName + "Enabled")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addStatement("return $T.isEnabled($T.class)", instrumentationClassName, typeClassName)
                .build();
    }

    private Result generateTypeWithMethods(String packageName, List<MethodSpec> methods, List<TypeElement> from) {
        String simpleName = "Instrumentations";
        TypeSpec typeSpec = TypeSpec.classBuilder(simpleName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethods(methods)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

        return new Result(packageName + "." + simpleName, javaFile, from.toArray(new TypeElement[0]));
    }
}
