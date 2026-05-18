package umbra.animation;

public record AnimationFrameEvent(
        int frameIndex,
        String id
) {
    public AnimationFrameEvent {
        if (frameIndex < 0) {
            throw new IllegalArgumentException("frameIndex must not be negative");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }
}
