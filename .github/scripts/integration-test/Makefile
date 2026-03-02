MAKEFLAGS += --no-print-directory
.SHELLFLAGS = -euc
SHELL = /bin/bash

elastic-start-local:  ## Start ES + EDOT Collector locally
	curl -fsSL https://elastic.co/start-local | sh -s -- --esonly --edot

.PHONY: start-env
start-env: elastic-start-local  ## Start the full environment

.PHONY: run-tests
run-tests: elastic-start-local ## Run integration tests
	. elastic-start-local/.env; \
	./integration_test.sh $$ES_LOCAL_URL $$ES_LOCAL_API_KEY

.PHONY: clean
clean: ## Stop and uninstall
	yes | elastic-start-local/stop.sh
	yes | elastic-start-local/uninstall.sh
	rm -rf elastic-start-local
