package name.modid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChyStateTest {
    @Test
    void buttonsAndSneakingOperateIndependently() {
        ChyState state = new ChyState();
        state.leftClick.start(2);
        state.rightClick.start(3);
        state.toggleSneaking();
        for (int tick = 1; tick <= 6; tick++) {
            assertEquals(tick % 2 == 0, state.leftClick.tick());
            assertEquals(tick % 3 == 0, state.rightClick.tick());
        }
        state.leftClick.toggle();
        assertTrue(state.rightClick.isEnabled());
        assertTrue(state.isSneaking());
        state.toggleSneaking();
        assertFalse(state.isSneaking());
        assertFalse(state.leftClick.isEnabled());
        assertTrue(state.rightClick.isEnabled());
    }

    @Test
    void disconnectResetsAllFeaturesAndPartialCountdowns() {
        ChyState state = new ChyState();
        state.leftClick.start(2);
        state.rightClick.start(3);
        state.leftClick.tick();
        state.rightClick.tick();
        state.toggleSneaking();
        state.reset();
        assertFalse(state.leftClick.isEnabled());
        assertFalse(state.rightClick.isEnabled());
        assertFalse(state.isSneaking());
        for (ClickState click : new ClickState[]{state.leftClick, state.rightClick}) {
            assertFalse(click.tick());
            click.toggle();
            assertEquals(12, click.intervalTicks());
            for (int tick = 1; tick < 12; tick++) assertFalse(click.tick());
            assertTrue(click.tick());
        }
    }
}
