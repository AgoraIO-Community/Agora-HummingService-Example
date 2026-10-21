# Humming Service Example

## Overview

Connect Agora RTC media with the Humming service example for specialized audio processing workflows.

Repository: [AgoraIO-Community/Agora-HummingService-Example](https://github.com/AgoraIO-Community/Agora-HummingService-Example)

## Prerequisites and Setup

1. Open `Android/Agora-HummingService-Example` in Android Studio.
2. Obtain the compatible Humming Service SDK and place its `.aar` files in that project's `app/libs/`, as required by the Android README.
3. Set `APP_ID` and `APP_CERTIFICATE` in the Android project's `local.properties`, sync Gradle and run the example.
4. For the iOS variant, inspect the included `iOS/` workspace and its dependencies before configuring signing and service credentials.

## What To Check

- Confirm that the Humming Service initializes and returns results for the sample's audio input.
- Exercise stop/start and permission denial; verify that service and microphone resources are released.

## Integration Notes

The SDK download is a prerequisite, not an artifact installed by this recipe. Confirm access to the historical service and SDK before attempting a device run.

Keep App Certificates, customer secrets and privileged token generation on a trusted backend for production. Use only your own test credentials while configuring historical demos; do not commit them. Preserve the sample's existing dependency versions and lifecycle until a separate migration is tested.

## Source References

- [Android/Agora-HummingService-Example/README.md](../../Android/Agora-HummingService-Example/README.md)
- [iOS/AgoraHumming-Example.xcworkspace/contents.xcworkspacedata](../../iOS/AgoraHumming-Example.xcworkspace/contents.xcworkspacedata)

## Validation Status

This recipe was checked against the repository's source and setup documents. The platform build and live service/device flow have not been run as part of this documentation change. Complete the checks above in an environment with the required SDKs, hardware and service access before treating it as a validated deployment.

## Implementation Prompt

```text
You are working in AgoraIO-Community/Agora-HummingService-Example. Use docs/ai/RECIPE.md and its linked source files to adapt the Humming Service Example reference to this project.

Start by inspecting the actual sample directory and configuration described above. Resolve its listed missing prerequisites before claiming it is runnable. Preserve the existing architecture and compatible dependency versions. Implement the recipe's feature-specific flow, permissions and resource cleanup. Use a trusted backend for privileged credentials and tokens in production.

Run the repository-specific build or launch flow, then perform each What To Check item. Report the commands run, the observed results and anything blocked by service access, hardware or incomplete sample code. Do not claim live validation from a documentation or catalog build alone.
```
