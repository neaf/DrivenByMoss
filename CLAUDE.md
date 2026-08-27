# DrivenByMoss – Claude Code notes

## Project basics

- **Language / build**: Java, Gradle. Run `./gradlew jar` to build. The output `.bwextension` file is loaded by Bitwig Studio.
- **Push 2 package**: `src/main/java/de/mossgrabers/controller/ableton/push/`

## Key architecture concepts

### Views vs Modes

Two independent active registries coexist at all times:

- **Views** (`ViewManager`) – full-screen pad layouts. Examples: `Views.SCENE_PLAY`, `Views.PLAY`, `Views.DRUM`. Check with `surface.getViewManager().isActive(Views.X)`.
- **Modes** (`ModeManager`) – parameter panels rendered on the display / knobs. Examples: `Modes.TRACK`, `Modes.VOLUME`, `Modes.SESSION`. Check with `surface.getModeManager().isActive(Modes.X)`.

A view *and* a mode are always both active simultaneously — they are orthogonal layers.

### "Mix" view

There is no `MixView` class. "Mix" is a mode: either `SessionMode` (clips/scenes) or track parameter modes (`VOLUME`, `PAN`, `SEND1–8`, etc.). The Mix/Track button is wired to `TrackCommand`. The predicate `Modes.isMixMode()` covers all of them.

### "Play mode"

"Play mode" on Push 2 means the **`Views.SCENE_PLAY`** view is active (not a mode). Detected with:
```java
surface.getViewManager().isActive(Views.SCENE_PLAY)
```

## Push 2 entry point

`PushControllerSetup.java` — wires every button, encoder, mode, and view.

### Button → command wiring pattern
```java
this.addButton(ButtonID.ARROW_UP, "Up", new PushCursorCommand(Direction.UP, model, surface), ...);
```

## Cursor buttons

Handled by `PushCursorCommand` (extends `CursorCommand` → `ModeCursorCommand`). Four instances created in `PushControllerSetup`, one per direction. Override `scrollUp/Down/Left/Right()` and `updateArrowStates()`.

## Configuration options (`PushConfiguration.java`)

Full pattern to add a new option:

1. **Constant** (after existing `NEXT_SETTING_ID + N` entries, currently highest is `+43` = `AUDIO_OUTPUTS`):
   ```java
   public static final Integer MY_SETTING = Integer.valueOf(NEXT_SETTING_ID + 44);
   ```
2. **Backing field** with default:
   ```java
   private boolean mySetting = false;
   ```
3. **Getter**:
   ```java
   public boolean isMySetting() { return this.mySetting; }
   ```
4. **UI registration** inside `activateCursorKeysSettings()` (or a dedicated method called from `init()`):
   ```java
   settingsUI.getEnumSetting("Label", CATEGORY_WORKFLOW, ON_OFF_OPTIONS, ON_OFF_OPTIONS[0])
       .addValueObserver(value -> {
           this.mySetting = ON_OFF_OPTIONS[1].equals(value);
           this.notifyObservers(MY_SETTING);
       });
   ```
   `ON_OFF_OPTIONS[0]` = `"Off"`, `ON_OFF_OPTIONS[1]` = `"On"`.

## Clip navigation API

Obtained via `model.getClipLauncherNavigator()` → `IClipLauncherNavigator`:

```java
void navigateClips(boolean isLeft);    // previous/next clip in the focused track
void navigateTracks(boolean isLeft);   // previous/next track (preserves slot selection)
void navigateScenes(boolean isLeft);   // previous/next scene
```

Already used by `SessionMode` (line ~71) for the Push 3 encoder.

## LED arrow states

Set in `updateArrowStates()`:
```java
this.scrollStates.setCanScrollUp(bool);
this.scrollStates.setCanScrollDown(bool);
this.scrollStates.setCanScrollLeft(bool);
this.scrollStates.setCanScrollRight(bool);
```

Use `IBank.canScrollBackwards()` / `canScrollForwards()` — these are **selection-aware**: they check whether a selected item exists and whether there is an item before/after it (including across page boundaries). Works for both slots and tracks:

```java
model.getSlotBank(16).canScrollBackwards()    // is there a clip before the selected one?
model.getCurrentTrackBank().canScrollForwards() // is there a track after the current one?
```

`canScrollForwards()` uses `doesExist()` only for the immediately next slot — empty slots still navigate fine since navigateClips moves the selection regardless of slot content.

## Useful file paths

| Concern | Path |
|---|---|
| Push 2 setup / wiring | `controller/ableton/push/PushControllerSetup.java` |
| Cursor button command | `controller/ableton/push/command/trigger/PushCursorCommand.java` |
| Configuration options | `controller/ableton/push/PushConfiguration.java` |
| Session/Mix mode | `controller/ableton/push/mode/SessionMode.java` |
| Clip navigator interface | `framework/daw/IClipLauncherNavigator.java` |
| Bank scroll helpers | `framework/daw/data/bank/AbstractItemBank.java` |
| View IDs | `framework/view/Views.java` |
| Mode IDs | `framework/mode/Modes.java` |
