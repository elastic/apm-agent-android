package co.elastic.apm.compile.tools.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class BaseNoticePlugin implements Plugin<Project> {

    protected static final String CONFIGURATION_LICENSES_COLLECTOR = "licencedDependencies";
    protected static final String LIBRARY_ELEMENT_LICENSED_DEPENDENCIES = "licensed-dependencies";
    protected static final String CONFIGURATION_NOTICES_COLLECTOR = "noticeFiles";
    protected static final String LIBRARY_ELEMENT_NOTICE_FILES = "notice-files";
    protected static final String TASK_CREATE_NOTICE_FILE_NAME = "createNoticeFile";
}
