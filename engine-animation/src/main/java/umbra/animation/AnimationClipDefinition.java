package umbra.animation;

/**
 * One animation clip backed by a horizontal sprite sheet.
 *
 * @param id gameplay-facing animation key
 * @param sheetPath project-relative PNG sheet path
 * @param frameCount number of frames in the sheet
 * @param fps playback rate
 * @param loop whether playback wraps after the final frame
 */
public record AnimationClipDefinition(String id, String sheetPath, int frameCount, int fps, boolean loop) {
    public AnimationClipDefinition {
        if (id == null || id.isBlank()) {
            throw new AnimationValidationException("clip id must not be blank");
        }
        if (sheetPath == null || sheetPath.isBlank()) {
            throw new AnimationValidationException("sheetPath must not be blank for clip: " + id);
        }
        if (frameCount <= 0) {
            throw new AnimationValidationException("frameCount must be positive for clip: " + id);
        }
        if (fps <= 0) {
            throw new AnimationValidationException("fps must be positive for clip: " + id);
        }
    }
}
