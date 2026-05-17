package umbra.animation;

import java.util.Objects;

/**
 * Deterministic clip player. Rendering reads the current clip and frame index;
 * gameplay remains independent from rendering backend details.
 */
public final class AnimationPlayer {
    private AnimationClipDefinition clip;
    private float elapsedSeconds;
    private int frameIndex;
    private boolean finished;

    public void play(AnimationClipDefinition nextClip) {
        Objects.requireNonNull(nextClip, "nextClip must not be null");
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
        if (deltaSeconds < 0.0f) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }
        if (clip == null || finished && !clip.loop()) {
            return;
        }

        elapsedSeconds += deltaSeconds;
        float frameDuration = 1.0f / clip.fps();
        int advancedFrame = (int) (elapsedSeconds / frameDuration);
        if (clip.loop()) {
            frameIndex = advancedFrame % clip.frameCount();
            return;
        }

        frameIndex = Math.min(advancedFrame, clip.frameCount() - 1);
        finished = advancedFrame >= clip.frameCount() - 1;
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
