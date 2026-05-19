package umbra.progression;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AbilityStateTest {
    @Test
    void unlockTracksAbilityOnceInOrder() {
        AbilityState state = new AbilityState();

        assertTrue(state.unlock("dash"));
        assertFalse(state.unlock("dash"));
        state.unlock("double_jump");

        assertTrue(state.has("dash"));
        assertEquals(List.of("dash", "double_jump"), state.unlockedAbilityIds());
    }

    @Test
    void rejectsBlankAbilityIds() {
        AbilityState state = new AbilityState();

        assertThrows(IllegalArgumentException.class, () -> state.unlock(""));
        assertThrows(IllegalArgumentException.class, () -> state.has(" "));
    }
}
