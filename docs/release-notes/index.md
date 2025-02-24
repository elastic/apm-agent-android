---
navigation_title: "Elastic APM Android Agent"
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/release-notes.html
  - https://www.elastic.co/guide/en/apm/agent/android/current/release-notes-0.x.html
---

# Elastic APM Android Agent release notes

Review the changes, fixes, and more in each version of Elastic APM Android Agent.

To check for security updates, go to [Security announcements for the Elastic stack](https://discuss.elastic.co/c/announcements/security-announcements/31).

::::{warning}
This functionality is in technical preview and may be changed or removed in a future release. Elastic will work to fix any issues, but features in technical preview are not subject to the support SLA of official GA features.
::::

% Release notes includes only features, enhancements, and fixes. Add breaking changes, deprecations, and known issues to the applicable release notes sections.

% ## version.next [elastic-apm-android-agent-versionext-release-notes]
% **Release date:** Month day, year

% ### Features and enhancements [elastic-apm-android-agent-versionext-features-enhancements]

% ### Fixes [elastic-apm-android-agent-versionext-fixes]

## 0.20.0 [elastic-apm-android-agent-0200-release-notes]
**Release date:** July 29, 2024

### Features and enhancements [elastic-apm-android-agent-0200-features-enhancements]
* Bumping upstream libs: [#340](https://github.com/elastic/apm-agent-android/pull/340)

### Fixes [elastic-apm-android-agent-0200-fixes]
* Addressing TrueTime issue #339: [#340](https://github.com/elastic/apm-agent-android/pull/340)

## 0.19.0 [elastic-apm-android-agent-0190-release-notes]
**Release date:** May 30, 2024

### Fixes [elastic-apm-android-agent-0190-fixes]
* Bytecode instrumentation issue in #323: [#324](https://github.com/elastic/apm-agent-android/pull/324)

## 0.18.0 [elastic-apm-android-agent-0180-release-notes]
**Release date:** May 24, 2024

### Fixes [elastic-apm-android-agent-0180-fixes]
* HTTP exporting fix: [#319](https://github.com/elastic/apm-agent-android/pull/319)

## 0.17.0 [elastic-apm-android-agent-0170-release-notes]
**Release date:** May 17, 2024

### Features and enhancements [elastic-apm-android-agent-0170-features-enhancements]
* Adding consumer R8 rules to address R8 full mode: [#309](https://github.com/elastic/apm-agent-android/pull/309)

## 0.16.0 [elastic-apm-android-agent-0160-release-notes]
**Release date:** April 3, 2024

### Fixes [elastic-apm-android-agent-0160-fixes]
* Removing strict version constraint that prevented enforcing compileSdk > 33: [#292](https://github.com/elastic/apm-agent-android/pull/292)

## 0.15.0 [elastic-apm-android-agent-0150-release-notes]
**Release date:** March 11, 2024

### Features and enhancements [elastic-apm-android-agent-0150-features-enhancements]
* Added configuration to set a base OpenTelemetry Resource object: [#276](https://github.com/elastic/apm-agent-android/pull/276)

## 0.14.0 [elastic-apm-android-agent-0140-release-notes]
**Release date:** February 2, 2024

### Features and enhancements [elastic-apm-android-agent-0140-features-enhancements]
* Making OpenTelemetry Android the base project: [#268](https://github.com/elastic/apm-agent-android/pull/268)

## 0.13.1 [elastic-apm-android-agent-0131-release-notes]
**Release date:** January 18, 2024

### Fixes [elastic-apm-android-agent-0131-fixes]
* Fix for #254: [#261](https://github.com/elastic/apm-agent-android/pull/261)

## 0.13.0 [elastic-apm-android-agent-0130-release-notes]
**Release date:** December 12, 2023

### Features and enhancements [elastic-apm-android-agent-0130-features-enhancements]
* Making internal logs configurable: [#245](https://github.com/elastic/apm-agent-android/pull/245)

### Fixes [elastic-apm-android-agent-0130-fixes]
* Fix #242: [#244](https://github.com/elastic/apm-agent-android/pull/244)

## 0.12.0 [elastic-apm-android-agent-0120-release-notes]
**Release date:** November 21, 2023

### Features and enhancements [elastic-apm-android-agent-0120-features-enhancements]
* Adding exception event to failed http responses: [#237](https://github.com/elastic/apm-agent-android/pull/237)
* Upgrading Byte Buddy to 1.14.10: [#238](https://github.com/elastic/apm-agent-android/pull/238)

## 0.11.0 [elastic-apm-android-agent-0110-release-notes]
**Release date:** October 31, 2023

### Fixes [elastic-apm-android-agent-0110-fixes]
* Fix truetime duplicated classes: [#222](https://github.com/elastic/apm-agent-android/pull/222)

## 0.10.0 [elastic-apm-android-agent-0100-release-notes]
**Release date:** October 27, 2023

### Features and enhancements [elastic-apm-android-agent-0100-features-enhancements]
* Adding setExportProtocol configuration to choose between HTTP and gRPC, defaulting to gRPC: [#213](https://github.com/elastic/apm-agent-android/pull/213)
* Upgrading Byte Buddy version to 1.14.9: [#207](https://github.com/elastic/apm-agent-android/pull/207)
* Setting minimum AGP version to 7.4.0 to use the Gradle plugin: [#207](https://github.com/elastic/apm-agent-android/pull/207)
* Removing Gradle’s warning on missing serverUrl param: [#209](https://github.com/elastic/apm-agent-android/pull/209)
* Adding http response content length attr to okhttp spans: [#211](https://github.com/elastic/apm-agent-android/pull/211)
* Marking okhttp spans as failed when receiving an error response code: [#212](https://github.com/elastic/apm-agent-android/pull/212)
* Bumping OTel SDK to 1.31.0 and adding new semconv dependency: [#217](https://github.com/elastic/apm-agent-android/pull/217)

## 0.9.0 [elastic-apm-android-agent-090-release-notes]
**Release date:** October 16, 2023

### Features and enhancements [elastic-apm-android-agent-090-features-enhancements]
* Making Session ID generator configurable: [#178](https://github.com/elastic/apm-agent-android/pull/178)
* Adding sample rate support: [#179](https://github.com/elastic/apm-agent-android/pull/179)
* Adding support for AGP > 8: [#197](https://github.com/elastic/apm-agent-android/pull/197)
* Setting minimum AGP version to 7.4.0: [#197](https://github.com/elastic/apm-agent-android/pull/197)

## 0.8.0 [elastic-apm-android-agent-080-release-notes]
**Release date:** August 23, 2023

### Features and enhancements [elastic-apm-android-agent-080-features-enhancements]
* Updating OpenTelemetry SDK to 1.27.0 where logs are stable: [#168](https://github.com/elastic/apm-agent-android/pull/168)
* Updating OpenTelemetry SDK to 1.28.0 where the new disk buffering lib is present: [#170](https://github.com/elastic/apm-agent-android/pull/170)
* Adding network connectivity attributes to logs: [#173](https://github.com/elastic/apm-agent-android/pull/173)
* Adding local persistence/caching support: [#174](https://github.com/elastic/apm-agent-android/pull/174)

## 0.7.0 [elastic-apm-android-agent-070-release-notes]
**Release date:** June 2, 2023

### Features and enhancements [elastic-apm-android-agent-070-features-enhancements]
* Sending app’s versionCode in the `service.build` attribute: [#153](https://github.com/elastic/apm-agent-android/pull/153)
* Sending app’s lifecycle events: [#159](https://github.com/elastic/apm-agent-android/pull/159)
* Adding spans, logs and metrics filtering support: [#160](https://github.com/elastic/apm-agent-android/pull/160)

### Fixes [elastic-apm-android-agent-070-fixes]
* Fix #164: [#165](https://github.com/elastic/apm-agent-android/pull/165)

## 0.6.0 [elastic-apm-android-agent-060-release-notes]
**Release date:** April 5, 2023

### Features and enhancements [elastic-apm-android-agent-060-features-enhancements]
* Using minSdk 24: [#149](https://github.com/elastic/apm-agent-android/pull/149)
* Making OpenTelemetry processors and exporters configurable: [#151](https://github.com/elastic/apm-agent-android/pull/151)
* Making environment name configurable at runtime: [#152](https://github.com/elastic/apm-agent-android/pull/152)

## 0.5.0 [elastic-apm-android-agent-050-release-notes]
**Release date:** April 3, 2023

### Features and enhancements [elastic-apm-android-agent-050-features-enhancements]
* Added server ApiKey auth support: [#141](https://github.com/elastic/apm-agent-android/pull/141)

## 0.4.0 [elastic-apm-android-agent-040-release-notes]
**Release date:** March 30, 2023

### Features and enhancements [elastic-apm-android-agent-040-features-enhancements]
* Wrapping HTTP spans: [#106](https://github.com/elastic/apm-agent-android/pull/106)
* Tracking app launch time metrics: [#110](https://github.com/elastic/apm-agent-android/pull/110)
* Added runtime configuration options: [#122](https://github.com/elastic/apm-agent-android/pull/122)
* Added central configuration [recording](https://github.com/elastic/apm/blob/main/specs/agents/mobile/configuration.md#recording-configuration) option: [#136](https://github.com/elastic/apm-agent-android/pull/136)

## 0.1.0 [elastic-apm-android-agent-010-release-notes]
**Release date:** December 12, 2022

### Features and enhancements [elastic-apm-android-agent-010-features-enhancements]
* OpenTelemetry agent set up.
* Automatic instrumentation of Android Activities and Fragments.
* Automatic instrumentation of OkHttp client calls.
* Filtering of http-related Spans

