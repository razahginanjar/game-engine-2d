package umbra.animation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AnimationMetadataLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsAndValidatesSpriteSheetMetadata() throws IOException {
        Files.createDirectories(tempDir.resolve("player"));
        ImageIO.write(new BufferedImage(120, 16, BufferedImage.TYPE_INT_ARGB), "png", tempDir.resolve("player/idle.png").toFile());

        AnimationMetadataLoader loader = new AnimationMetadataLoader();
        AnimationSetDefinition set = loader.load(new StringReader("""
                {
                  "asset_id": "player_test",
                  "frame_width": 24,
                  "frame_height": 16,
                  "pivot": "bottom_center",
                  "collision_box": { "x": 8, "y": 2, "w": 10, "h": 12 },
                  "hurtbox": { "x": 7, "y": 1, "w": 12, "h": 14 },
                  "clips": [
                    { "id": "idle", "sheet": "player/idle.png", "frames": 5, "fps": 12, "loop": true }
                  ]
                }
                """));

        loader.validateSheets(set, tempDir, Set.of("idle"));

        assertEquals("player_test", set.assetId());
        assertEquals(1, set.clips().size());
    }

    @Test
    void rejectsMissingRequiredClip() throws IOException {
        Files.createDirectories(tempDir.resolve("player"));
        ImageIO.write(new BufferedImage(24, 16, BufferedImage.TYPE_INT_ARGB), "png", tempDir.resolve("player/idle.png").toFile());
        AnimationSetDefinition set = new AnimationMetadataLoader().load(new StringReader("""
                {
                  "asset_id": "player_test",
                  "frame_width": 24,
                  "frame_height": 16,
                  "pivot": "bottom_center",
                  "clips": [
                    { "id": "idle", "sheet": "player/idle.png", "frames": 1, "fps": 12, "loop": true }
                  ]
                }
                """));

        assertThrows(AnimationValidationException.class, () -> new AnimationMetadataLoader().validateSheets(set, tempDir, Set.of("run")));
    }

    @Test
    void rejectsWrongSheetDimensions() throws IOException {
        Files.createDirectories(tempDir.resolve("player"));
        ImageIO.write(new BufferedImage(100, 16, BufferedImage.TYPE_INT_ARGB), "png", tempDir.resolve("player/idle.png").toFile());
        AnimationSetDefinition set = new AnimationMetadataLoader().load(new StringReader("""
                {
                  "asset_id": "player_test",
                  "frame_width": 24,
                  "frame_height": 16,
                  "pivot": "bottom_center",
                  "clips": [
                    { "id": "idle", "sheet": "player/idle.png", "frames": 5, "fps": 12, "loop": true }
                  ]
                }
                """));

        assertThrows(AnimationValidationException.class, () -> new AnimationMetadataLoader().validateSheets(set, tempDir, Set.of("idle")));
    }
}
