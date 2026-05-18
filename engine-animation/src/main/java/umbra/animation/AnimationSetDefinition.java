package umbra.animation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnimationSetDefinition {
    private final String id;
    private final List<AnimationClipDefinition> clips;
    private final Map<String, AnimationClipDefinition> clipsById;

    public AnimationSetDefinition(String id, List<AnimationClipDefinition> clips) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (clips == null || clips.isEmpty()) {
            throw new IllegalArgumentException("clips must not be empty");
        }
        this.id = id;
        this.clips = List.copyOf(clips);
        this.clipsById = new HashMap<>();
        for (AnimationClipDefinition clip : this.clips) {
            AnimationClipDefinition previous = clipsById.put(clip.id(), clip);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate clip id: " + clip.id());
            }
        }
    }

    public String id() {
        return id;
    }

    public List<AnimationClipDefinition> clips() {
        return clips;
    }

    public AnimationClipDefinition clip(String id) {
        AnimationClipDefinition clip = clipsById.get(id);
        if (clip == null) {
            throw new IllegalArgumentException("unknown clip id: " + id);
        }
        return clip;
    }

    public boolean hasClip(String id) {
        return id != null && clipsById.containsKey(id);
    }
}
