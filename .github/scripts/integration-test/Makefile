MAKEFLAGS += --no-print-directory
.SHELLFLAGS = -euc
SHELL = /bin/bash

elastic-start-local:  ## Start ES locally
	curl -fsSL https://elastic.co/start-local | sh -s -- --esonly -v 9.2.0

.PHONY: start-edot-collector
demo_app_dir="build/demo-app"
edot_collector_build="$(demo_app_dir)/edot-collector/build"
start-edot-collector: elastic-start-local ## Start EDOT Collector
	git clone https://github.com/elastic/android-agent-demo.git $(demo_app_dir)
	. elastic-start-local/.env; \
	echo "endpoint=$$ES_LOCAL_URL\napi_key=$$ES_LOCAL_API_KEY" > "$(demo_app_dir)/elasticsearch.properties"

    # Preparing collector binary
	$(demo_app_dir)/gradlew -p "$(demo_app_dir)" :edot-collector:prepareEdotCollector

    # Launching EDOT Collector
	"$(edot_collector_build)/bin/otelcol" --config "$(edot_collector_build)/configuration/edot-configuration.yml" &

    # Waiting for EDOT Collector to run
	./await_port.sh 4318 30

.PHONY: run-tests
run-tests: elastic-start-local ## Run integration tests
	. elastic-start-local/.env; \
	./integration_test.sh $$ES_LOCAL_URL $$ES_LOCAL_API_KEY

.PHONY: clean
clean: clean-es clean-edot-collector

.PHONY: clean-es
clean-es: ## Stop and uninstall ES
	yes | elastic-start-local/stop.sh
	yes | elastic-start-local/uninstall.sh
	rm -rf elastic-start-local

.PHONY: clean-edot-collector
clean-edot-collector: ## Stop and cleans EDOT Collector
	kill $$(lsof -t -i:4318)
	rm -rf $(demo_app_dir)