# Component stability

The agent consists of multiple components and features designed for flexibility and adaptability to a variety of use cases. While most of these features are stable, the continued evolution of the agent requires the introduction of new, experimental features. These features may not yet be production-ready but are made available for testing and feedback, helping to ensure their reliability and robustness once they reach stable status.

This page outlines the different stability levels assigned to the agent’s features and explains what each level represents.

## Quick summary

| Status level         | Description                                               |
|----------------------|-----------------------------------------------------------|
| No explicit "Status" | Equivalent to Stable                                      |
| Technical Preview         | Breaking changes and even removal of features are allowed |
| Stable               | Breaking changes are not allowed                          |

## Statuses

### Technical Preview

Features and components classified as technical preview are explicitly labeled with a status line beneath their title in this documentation, which mentions it. Additionally, standalone components at this level include an `-alpha` suffix in their version identifiers.

At this stability level, breaking changes may occur in any release. In some cases, development of a technical preview feature may be discontinued, and the feature may be removed in a minor version update. For these reasons, technical preview features are not recommended for use in production environments.

### Stable

This is the default status.

Features marked as stable are fully maintained and supported in accordance with the [Elastic's version policy](https://www.elastic.co/support/eol). At this stability level, no breaking changes will be introduced in minor or patch releases of the associated components and features.
