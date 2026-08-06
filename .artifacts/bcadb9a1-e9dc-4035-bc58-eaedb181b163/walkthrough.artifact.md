# Walkthrough - Join Room and Room State Synchronization

I have implemented the logic for guests to join a room with a custom display name and synchronized the room state (participant list and ready status) across all connected devices using Sockets.

## Changes Made

### 1. Data Models
- **[Participant.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/model/Participant.kt)**: Introduced a new model to track individual users, their host status, and their ready state.
- **[Room.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/model/Room.kt)**: Updated to use a list of `Participant` objects instead of simple strings.

### 2. Networking Logic
- **[SocketServer.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/network/socket/SocketServer.kt)**:
    - The host now manages the official participant list.
    - When a user joins or leaves, the host updates its local state and broadcasts the full `PARTICIPANT_LIST_UPDATED` message to all guests.
    - Added logic to detect client disconnections and automatically update the room state.
- **[SocketManager.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/network/socket/SocketManager.kt)**: Exposed `clientId` to track local user identity across different screens.

### 3. ViewModel and State Management
- **[RoomViewModel.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/viewmodel/RoomViewModel.kt)**:
    - Implemented a handler for `PARTICIPANT_LIST_UPDATED` which refreshes the UI state for guests.
    - Added `toggleReady()` to allow guests to signal they are ready to watch.
    - Implemented host-side logic to process `JOIN_ROOM`, `LEAVE_ROOM`, and `USER_READY` messages.

### 4. UI Enhancements
- **[JoinRoomScreen.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/ui/screens/room/JoinRoomScreen.kt)**:
    - Added a text field for guests to enter their display name.
    - The connection is now initiated only after the user provides a name.
- **[WaitingRoomScreen.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/ui/screens/room/WaitingRoomScreen.kt)**:
    - The participant list now shows real-time status: Green icons for "Ready", Gray for "Not Ready".
    - Guests have a "I'm Ready!" / "I'm Not Ready" toggle button.
    - The Host's "Start Watching" button is dynamically enabled only when all guests are ready and at least one guest is present.

## Message Flow
1. **Guest** $\rightarrow$ `JOIN_ROOM` (Payload: Display Name) $\rightarrow$ **Host**.
2. **Host** $\rightarrow$ Updates internal list $\rightarrow$ Broadcasts `PARTICIPANT_LIST_UPDATED` (Payload: Full JSON List) $\rightarrow$ **All Guests**.
3. **Guest** $\rightarrow$ `USER_READY` (Payload: true/false) $\rightarrow$ **Host**.
4. **Host** $\rightarrow$ Updates internal list $\rightarrow$ Broadcasts `PARTICIPANT_LIST_UPDATED` $\rightarrow$ **All Guests**.

## Verification Results
- **Build**: Successfully built the project using `:app:assembleDebug`.
- **Logic**: Synchronization is achieved by making the Host device the source of truth for the room state.
