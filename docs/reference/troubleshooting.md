# Troubleshooting

## General

The agent creates logs that enable you to see what it is working on and what might have failed at some point. You can find those in [logcat](https://developer.android.com/studio/debug/logcat), filtered by the tag `ELASTIC_AGENT`.

For more information about the agent's internal logs, as well as how to configure them, refer to the [internal logging policy](configuration.md#internal-logging-policy) configuration.

## Connectivity to the Elastic Stack

If after following the [getting started](getting-started.md) guide and configuring your Elastic Stack [endpoint parameters](configuration.md#export-connectivity), you can't see your application's data in Kibana, you can follow the following tips to try and figure out what could be wrong.

### Checking out logs

The agent prints debug logs, which can be seen in [logcat](https://developer.android.com/studio/debug/logcat), using the tag `ELASTIC_AGENT`, where you can have a look at your endpoint configuration parameters with a log that reads: _"Initializing connectivity with config [your endpoint configuration]"_. Take a look at those and make sure that the provided configuration matches your Elastic Stack endpoint parameters.

### Inspecting network traffic

You can take a look at your app's outgoing network requests via Android Studio's [network inspector tool](http://developer.android.com/studio/debug/network-profiler). This tool can show you the agent's export requests, where you can see if they were successful or not, as well as the request body and the Elastic Stack response body for when you need more details of the whole process. Apart from that, this tool also provides a way to export a file with the information of all of your app's HTTP requests, which you could share with our support team if needed.

### SSL/TLS error

Sometimes the request to the Elastic Stack endpoint won't show up in the [network inspector](#inspecting-network-traffic). A common issue when this happens is that there is an SSL/TLS error that occurs when the agent tries to contact your Elastic Stack endpoint. This is often the case when you work with an on-prem Elastic Stack that doesn't have trusted CAs, for which you'd need to add custom security configurations to your app to make the export work. Take a look at [how to configure SSL/TLS](how-tos.md#how-to-configure-ssltls) for more information.
