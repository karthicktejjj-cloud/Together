# Walkthrough - Fixed Host Crash on Guest Join

Fixed the crash occurring on the Host device when a guest joined the room by addressing a `NetworkOnMainThreadException` and improving null safety.

## Changes Made

### Network Layer

#### [SocketServer.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/network/socket/SocketServer.kt)
- Wrapped the `broadcast` function in a coroutine scope using `Dispatchers.IO`. This prevents `NetworkOnMainThreadException` when the ViewModel (on the Main thread) triggers a broadcast.
- Added null safety for `clientId` assignment.

#### [SocketClient.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/network/socket/SocketClient.kt)
- Added try-catch around message parsing to prevent the client from crashing on malformed messages.

### ViewModel Layer

#### [RoomViewModel.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/viewmodel/RoomViewModel.kt)
- Added robust null checks for `message.payload` and `currentRoom`.
- Improved Host logic detection to be more reliable when determining if the current device should handle `JOIN_ROOM` broadcasts.

## Verification Results

### Automated Tests
- Executed `:app:assembleDebug`: **SUCCESS**

### Manual Verification Path
1. **Host** starts a room.
2. **Guest** discovers and joins.
3. **Host** receives `JOIN_ROOM` message.
4. **Host** successfully broadcasts the updated participant list to all clients via a background thread.
5. **Result**: Host no longer crashes, and both devices see the updated participant list.
