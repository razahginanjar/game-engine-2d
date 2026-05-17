package umbra.room;

public final class RoomValidationException extends RuntimeException {
    public RoomValidationException(String message) {
        super(message);
    }

    public RoomValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
