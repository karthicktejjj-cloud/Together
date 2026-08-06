# Implementation Plan - Fix Host Crash on Guest Join

Fix the crash occurring on the Host device immediately after a guest joins the room.

## User Review Required

> [!IMPORTANT]
> The primary cause of the crash is identified as a `NetworkOnMainThreadException` due to broadcasting messages on the Main thread. I will also address potential `NullPointerException` risks during message parsing.

## Proposed Changes

### Network Layer

#### [MODIFY] [SocketServer.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/network/socket/SocketServer.kt)
- Wrap `broadcast` logic in `scope.launch` to ensure it runs on `Dispatchers.IO`.
- Add a null check for `message.sender` before using it as a key in `clients` map.
- Add more robust error logging in `handleClient`.

#### [MODIFY] [SocketClient.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/network/socket/SocketClient.kt)
- Minor cleanup and ensured error handling.

### ViewModel Layer

#### [MODIFY] [RoomViewModel.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/viewmodel/RoomViewModel.kt)
- Add null safety checks for `message.payload` during JSON deserialization.
- Add safety check for `currentRoom` being null when processing `JOIN_ROOM`.

## Verification Plan

### Automated Tests
- Run `gradle build` to verify the project still compiles correctly.

### Manual Verification
- Verify that `SocketServer.broadcast` now consistently uses a background thread.
- Verify that `JOIN_ROOM` handling on the Host is now guarded against null states.
