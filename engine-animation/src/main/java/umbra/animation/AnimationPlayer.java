package umbra.animation;

/**
 * Deterministic clip player. Rendering code can query the current frame index
 * and resolve it against the validated sheet metadata.
 */
public final class AnimationPlayer {
    private AnimationClipDefinition clip;
    private float elapsedSeconds;
    private int frameIndex;
    private boolean finished;

    public void play(AnimationClipDefinition nextClip) {
        if (clip != null && clip.id().equals(nextClip.id())) {
            return;
        }
        clip = nextClip;
        elapsedSeconds = 0.0f;
        frameIndex = 0;
        finished = false;
    }

    public void restart(AnimationClipDefinition nextClip) {
        clip = null;
        play(nextClip);
    }

    public void update(float deltaSeconds) {
        if (clip == null || finished && !clip.loop()) {
            return;
        }
        if (deltaSeconds < 0.0f) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }

        elapsedSeconds += deltaSeconds;
        float frameDuration = 1.0f / clip.fps();
        int advancedFrame = (int) (elapsedSeconds / frameDuration);

        if (clip.loop()) {
            frameIndex = advancedFrame % clip.frameCount();
        } else {
            frameIndex = Math.min(advancedFrame, clip.frameCount() - 1);
            finished = advancedFrame >= clip.frameCount() - 1;
        }
    }

    public AnimationClipDefinition clip() {
        return clip;
    }

    public int frameIndex() {
        return frameIndex;
    }

    public boolean finished() {
        return finished;
    }
}
