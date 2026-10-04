package name.modid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AutoAttackStateTest {
    @Test
    void customIntervalIsForgottenAfterToggleOffAndOn() {
        AutoAttackState state = new AutoAttackState();
        state.start(2.0);
        assertEquals(40, state.intervalTicks());
        state.toggle();
        assertFalse(state.isEnabled());
        state.toggle();
        assertTrue(state.isEnabled());
        assertEquals(12, state.intervalTicks());
        assertEquals(0.6, state.intervalSeconds());
    }

    @Test
    void attacksOccurOnlyAfterEachFullInterval() {
        AutoAttackState state = new AutoAttackState();
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
        AutoAttackState state = new AutoAttackState();
        state.toggle();
        for (int i = 0; i < 11; i++) state.tick();
        state.start(0.1);
        assertFalse(state.tick());
        assertTrue(state.tick());
        state.tick();
        state.stop();
        state.toggle();
        for (int i = 0; i < 11; i++) assertFalse(state.tick());
        assertTrue(state.tick());
    }

    @Test
    void secondsRoundUpToActualWholeTickInterval() {
        AutoAttackState state = new AutoAttackState();
        double[] seconds = {0.01, 0.05, 0.06, 0.55, 0.6, 1.0, 10.0};
        int[] ticks = {1, 1, 2, 11, 12, 20, 200};
        for (int i = 0; i < seconds.length; i++) {
            state.start(seconds[i]);
            assertEquals(ticks[i], state.intervalTicks());
            assertEquals(ticks[i] / 20.0, state.intervalSeconds());
        }
    }

    @Test
    void invalidIntervalsDoNotEnableAttack() {
        AutoAttackState state = new AutoAttackState();
        for (double value : new double[]{0, -1, 0.009, 10.01, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> state.start(value));
            assertFalse(state.isEnabled());
        }
    }
}
