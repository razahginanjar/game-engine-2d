package umbra.save;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

public final class SaveGameCodec {
    private final Gson gson = new Gson();

    public String encode(SaveGame saveGame) {
        Objects.requireNonNull(saveGame, "saveGame must not be null");
        return gson.toJson(saveGame);
    }

    public void write(SaveGame saveGame, Writer writer) {
        Objects.requireNonNull(saveGame, "saveGame must not be null");
        Objects.requireNonNull(writer, "writer must not be null");
        gson.toJson(saveGame, writer);
    }

    public SaveGame decode(String json) {
        if (json == null || json.isBlank()) {
            throw new SaveValidationException("save json must not be blank");
        }
        try {
            return gson.fromJson(json, SaveGame.class);
        } catch (JsonSyntaxException exception) {
            throw new SaveValidationException("save json is invalid", exception);
        } catch (RuntimeException exception) {
            throw normalizeValidationException(exception);
        }
    }

    public SaveGame read(Reader reader) {
        Objects.requireNonNull(reader, "reader must not be null");
        try {
            return gson.fromJson(reader, SaveGame.class);
        } catch (JsonSyntaxException exception) {
            throw new SaveValidationException("save json is invalid", exception);
        } catch (RuntimeException exception) {
            throw normalizeValidationException(exception);
        }
    }

    private SaveValidationException normalizeValidationException(RuntimeException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof SaveValidationException saveValidationException) {
            return saveValidationException;
        }
        return new SaveValidationException("save json is invalid", exception);
    }
}
