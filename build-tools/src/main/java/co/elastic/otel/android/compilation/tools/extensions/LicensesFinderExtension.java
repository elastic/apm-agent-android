package co.elastic.otel.android.compilation.tools.extensions;

import org.gradle.api.file.RegularFileProperty;

public abstract class LicensesFinderExtension {

    public abstract RegularFileProperty getManualMappingFile();
}
