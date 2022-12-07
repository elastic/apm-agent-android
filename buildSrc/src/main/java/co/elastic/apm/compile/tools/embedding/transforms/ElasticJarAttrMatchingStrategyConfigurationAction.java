package co.elastic.apm.compile.tools.embedding.transforms;

import org.gradle.api.Action;
import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.AttributeMatchingStrategy;
import org.gradle.api.attributes.CompatibilityCheckDetails;

public class ElasticJarAttrMatchingStrategyConfigurationAction implements Action<AttributeMatchingStrategy<String>> {

    public void execute(AttributeMatchingStrategy<String> stringAttributeMatchingStrategy) {
        stringAttributeMatchingStrategy.getCompatibilityRules().add(ElasticJarRule.class);
    }

    public abstract static class ElasticJarRule implements AttributeCompatibilityRule<String> {

        public void execute(CompatibilityCheckDetails<String> details) {
            if (AarGradleTransformAction.ELASTIC_JAR.equals(details.getConsumerValue()) && "jar".equals(details.getProducerValue())) {
                details.compatible();
            }
        }
    }
}