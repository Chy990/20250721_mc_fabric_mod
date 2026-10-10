package name.modid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClickStateTest {
    @Test
    void customIntervalIsForgottenAfterToggleOffAndOn() {
        ClickState state = new ClickState();
        state.start(40);
        assertEquals(40, state.intervalTicks());
        state.toggle();
        assertFalse(state.isEnabled());
        state.toggle();
        assertTrue(state.isEnabled());
        assertEquals(12, state.intervalTicks());
    }

    @Test
    void attacksOccurOnlyAfterEachFullInterval() {
        ClickState state = new ClickState();
        assertFalse(state.tick());
        state.toggle();
        for (int cycle = 0; cycle < 3; cycle++) {
            for (int tick = 1; tick < 12; tick++) {
                assertFalse(state.tick(), "Must not attack early");
            }
            assertTrue(state.tick());
        }
        state.stop();
        for (int tick = 0; tick < 30; tick++) {
            assertFalse(state.tick());
        }
    }

    @Test
    void changingIntervalAndRestartingResetPartialCountdown() {
        ClickState state = new ClickState();
        state.toggle();
        for (int i = 0; i < 11; i++) state.tick();
        state.start(2);
        assertFalse(state.tick());
        assertTrue(state.tick());
        state.tick();
        state.stop();
        state.toggle();
        for (int i = 0; i < 11; i++) assertFalse(state.tick());
        assertTrue(state.tick());
    }

    @Test
    void tickIntervalsAreUsedExactlyIncludingOneTick() {
        ClickState state = new ClickState();
        for (int interval : new int[]{1, 2, 12, 20, 200, 201}) {
            state.start(interval);
            assertEquals(interval, state.intervalTicks());
            for (int cycle = 0; cycle < 2; cycle++) {
                for (int tick = 1; tick < interval; tick++) assertFalse(state.tick());
                assertTrue(state.tick());
            }
        }
        state.start(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, state.intervalTicks());
        assertFalse(state.tick());
    }

    @Test
    void invalidIntervalsDoNotEnableOrModifyTimer() {
        ClickState state = new ClickState();
        for (int value : new int[]{0, -1, Integer.MIN_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> state.start(value));
            assertFalse(state.isEnabled());
        }
        state.start(2);
        assertFalse(state.tick());
        assertThrows(IllegalArgumentException.class, () -> state.start(0));
        assertTrue(state.isEnabled());
        assertEquals(2, state.intervalTicks());
        assertTrue(state.tick(), "Invalid arguments must preserve the countdown");
    }
}
