# Getting started

## Requirements [gradle-requirements]

| Requirement           | Minimum version                                                                                           |
|-----------------------|-----------------------------------------------------------------------------------------------------------|
| Android Gradle plugin | 7.4.0                                                                                                     |
| Android API level     | 26 (or 21 with [desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring)) |

:::{important} - Apps with minSdk below 26
If your application's [minSdk](https://developer.android.com/studio/publish/versioning#minsdk) value is lower than 26, you **must** add [Java 8 desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring).

More info on [FAQs](faq.md#why-desugaring).
:::

## Gradle setup

