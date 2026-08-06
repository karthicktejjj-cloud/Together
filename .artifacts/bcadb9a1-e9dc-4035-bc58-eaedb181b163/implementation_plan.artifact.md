# Implementation Plan - Join Room and Room State Synchronization

Implement the logic to allow guests to join a room, synchronization of the participant list, and ready-state management using Sockets.

## User Review Required

> [!IMPORTANT]
> - I will be refactoring the `Room` model to use a `Participant` object instead of a simple `String` for the participant list.
> - The host will broadcast the full participant list to all clients whenever it changes (join/leave/ready toggle).
> - Guests will need to enter their name before joining a room.

## Proposed Changes

### [Component: Model]

#### [NEW] [Participant.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/model/Participant.kt)
- Data class: `id`, `name`, `isHost`, `isReady`.

#### [MODIFY] [Room.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/model/Room.kt)
- Change `participants: List<String>` to `participants: List<Participant>`.

### [Component: Networking - Socket]

#### [MODIFY] [MessageType.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/network/socket/MessageType.kt)
- Add `PARTICIPANT_LIST_UPDATED` and `USER_READY`.

#### [MODIFY] [SocketServer.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/network/socket/SocketServer.kt)
- Update `handleClient` to manage `Participant` objects.
- On `JOIN_ROOM`: Add participant, update list, broadcast `PARTICIPANT_LIST_UPDATED`.
- On `HEARTBEAT` (or timeout): Detect disconnects, remove participant, broadcast `PARTICIPANT_LIST_UPDATED`.
- On `USER_READY`: Update participant `isReady`, broadcast `PARTICIPANT_LIST_UPDATED`.

### [Component: ViewModel]

#### [MODIFY] [RoomViewModel.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/viewmodel/RoomViewModel.kt)
- Add logic to update `currentRoom` when `PARTICIPANT_LIST_UPDATED` is received.
- Add `toggleReadyStatus()` function.

#### [MODIFY] [RoomRepository.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/data/repository/RoomRepository.kt)
- Add `updateParticipants(List<Participant>)` to update the state.

### [Component: UI Screens]

#### [MODIFY] [JoinRoomScreen.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/ui/screens/room/JoinRoomScreen.kt)
- Add a text field for "Your Name".
- Delay `JOIN_ROOM` message until the user enters their name and clicks "Join".

#### [MODIFY] [WaitingRoomScreen.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/ui/screens/room/WaitingRoomScreen.kt)
- Update the participant list UI to show `isReady` status.
- Add a "Ready" button for guests.
- Enable "Start Watching" for the host only when at least one guest is connected.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.

### Manual Verification
- **Host**: Create a room.
- **Guest**: Discover the room, enter name, and join.
- **Sync**: Verify both devices see the updated participant list.
- **Ready State**: Toggle "Ready" on Guest and verify Host sees the change.
- **Disconnect**: Kill the Guest app and verify Host sees the participant removed.
