# Implementation Plan - Fix HTTP Streaming Root Cause

Fix the connection failure between Guest and Host by correctly handling the HTTP request lifecycle.

## Root Cause Analysis
The identified root cause is a **TCP Reset (RST)** triggered by the server.
1. The server reads only the first line of the HTTP request (`GET /video...`) and immediately starts sending the response.
2. The remaining request headers (User-Agent, Range, etc.) stay in the socket's receive buffer.
3. When the server finishes streaming and closes the socket while unread data exists in the buffer, the TCP stack sends an abortive **RST** packet instead of a clean **FIN**.
4. The Guest (ExoPlayer) receives the RST, which causes it to discard any data received so far and report "Unable to connect" or "Connection reset".

Additionally, the server does not handle `HEAD` requests, which many players use to probe for content length and type before starting the actual stream.

## Proposed Changes

### Network Layer

#### [MODIFY] [VideoServer.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/network/streaming/VideoServer.kt)
- **Consume Headers**: Update `handleClient` to read all request headers until an empty line is encountered.
- **Support HEAD**: Update request matching logic to accept both `GET` and `HEAD` requests.
- **Conditional Streaming**: Modify `streamVideo` to only send the file body if the request was a `GET`.

## Verification Plan

### Automated Tests
- Build the project using `gradle build`.

### Manual Verification
- **Host**: Start watching a video.
- **Guest**: Join and verify playback starts immediately.
- **Logs**: Verify `VideoServer` logs show headers being consumed and whether a `GET` or `HEAD` request was processed.
