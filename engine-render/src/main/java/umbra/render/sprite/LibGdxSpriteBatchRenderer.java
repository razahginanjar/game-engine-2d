package umbra.render.sprite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import umbra.render.debug.DebugColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * LibGDX adapter for sprite draw commands.
 */
public final class LibGdxSpriteBatchRenderer {
    private final SpriteBatch batch;
    private final Map<String, Texture> textures = new HashMap<>();

    public LibGdxSpriteBatchRenderer(SpriteBatch batch) {
        this.batch = Objects.requireNonNull(batch, "batch must not be null");
    }

    public void registerTexture(String textureId, Texture texture) {
        if (textureId == null || textureId.isBlank()) {
            throw new IllegalArgumentException("textureId must not be blank");
        }
        textures.put(textureId, Objects.requireNonNull(texture, "texture must not be null"));
    }

    public boolean hasTexture(String textureId) {
        return textures.containsKey(textureId);
    }

    public int textureWidth(String textureId) {
        Texture texture = textures.get(textureId);
        return texture == null ? 0 : texture.getWidth();
    }

    public int textureHeight(String textureId) {
        Texture texture = textures.get(textureId);
        return texture == null ? 0 : texture.getHeight();
    }

    public void render(SpriteDrawList drawList) {
        Objects.requireNonNull(drawList, "drawList must not be null");
        if (drawList.commands().isEmpty()) {
            return;
        }

        batch.begin();
        for (SpriteDrawCommand command : drawList.commands()) {
            Texture texture = textures.get(command.textureId());
            if (texture == null) {
                continue;
            }
            setColor(command.tint());
            batch.draw(
                    texture,
                    command.x(),
                    command.y(),
                    command.width(),
                    command.height(),
                    command.sourceX(),
                    command.sourceY(),
                    command.sourceWidth(),
                    command.sourceHeight(),
                    command.flipX(),
                    command.flipY()
            );
        }
        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        batch.end();
    }

    public void disposeTextures() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
    }

    private void setColor(DebugColor color) {
        batch.setColor(color.r(), color.g(), color.b(), color.a());
    }
}
