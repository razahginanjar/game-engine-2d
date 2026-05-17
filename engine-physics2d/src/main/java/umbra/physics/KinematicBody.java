package umbra.physics;

/**
 * Mutable kinematic body for platformer controllers.
 */
public final class KinematicBody {
    private float x;
    private float y;
    private final float width;
    private final float height;
    private float velocityX;
    private float velocityY;

    public KinematicBody(float x, float y, float width, float height) {
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("body size must be positive");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Aabb bounds() {
        return new Aabb(x, y, width, height);
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float velocityX() {
        return velocityX;
    }

    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    public float velocityY() {
        return velocityY;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    void translate(float dx, float dy) {
        x += dx;
        y += dy;
    }
}
