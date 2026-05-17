package umbra.physics;

/**
 * Axis-aligned rectangle in world pixels.
 *
 * @param x minimum x position
 * @param y minimum y position
 * @param width rectangle width in pixels
 * @param height rectangle height in pixels
 */
public record Aabb(float x, float y, float width, float height) {
    public Aabb {
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("AABB size must be positive");
        }
    }

    public float right() {
        return x + width;
    }

    public float top() {
        return y + height;
    }

    public Aabb translated(float dx, float dy) {
        return new Aabb(x + dx, y + dy, width, height);
    }

    public boolean overlaps(Aabb other) {
        return x < other.right()
                && right() > other.x
                && y < other.top()
                && top() > other.y;
    }
}
