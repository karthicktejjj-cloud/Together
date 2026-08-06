# Implementation Plan - Video Details Screen

Implement a new `VideoDetailsScreen` to show video metadata before playback, following the Home -> Local Videos -> Video Details -> Video Player flow.

## User Review Required

> [!IMPORTANT]
> I will be updating the `Video` model and `VideoRepository` to include resolution and file path information, as these were requested for the details screen but were missing from the initial implementation.

## Proposed Changes

### [Component: Model & Data]

#### [MODIFY] [Video.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/model/Video.kt)
- Add `resolution: String` and `path: String` fields to the `Video` data class.

#### [MODIFY] [VideoRepository.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/data/repository/VideoRepository.kt)
- Update MediaStore projection to include `RESOLUTION` and `DATA`.
- Map these new fields into the `Video` objects.

### [Component: UI Screens]

#### [NEW] [VideoDetailsScreen.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/ui/screens/videos/VideoDetailsScreen.kt)
- Create a Material 3 screen that displays the video details.
- Show a placeholder for the thumbnail (or actual thumbnail if feasible, but I'll start with a placeholder as requested).
- Implement "Play" and "Create Room" buttons.
- "Play" will navigate to `VideoPlayerScreen`.
- "Create Room" will navigate to a placeholder `CreateRoomScreen`.

#### [NEW] [CreateRoomScreen.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/ui/screens/room/CreateRoomScreen.kt)
- Simple placeholder screen for the "Create Room" feature.

#### [MODIFY] [VideoListScreen.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/videos/VideoListScreen.kt)
- Change navigation from `player/$encodedUri` to `details/$videoId`.

#### [DELETE] [VideoDetailsScreen.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/videos/VideoDetailsScreen.kt)
- Remove the misplaced/incomplete file.

### [Component: Navigation]

#### [MODIFY] [AppNavigation.kt](file:///C:/Users/ELCOT/AndroidStudioProjects/Together/app/src/main/java/com/together/app/ui/screens/splash/AppNavigation.kt)
- Add routes for `details/{videoId}` and `create_room`.
- Pass `videoId` to `VideoDetailsScreen`.

## Verification Plan

### Automated Tests
- Build the project using `./gradlew :app:assembleDebug`.
- Fix any compilation errors related to the model change.

### Manual Verification
- Verify navigation from Video List to Video Details.
- Verify that Video Details shows the correct title, duration, size, resolution, and path.
- Verify navigation from Video Details to Video Player.
- Verify navigation from Video Details to Create Room placeholder.
