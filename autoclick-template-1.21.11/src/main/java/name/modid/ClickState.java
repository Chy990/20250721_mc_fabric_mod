package name.modid;

/** Independent tick-based timer for either automatic mouse button. */
public final class ClickState {
    public static final int DEFAULT_INTERVAL_TICKS = 12;
    private boolean enabled;
    private int intervalTicks = DEFAULT_INTERVAL_TICKS;
    private int elapsedTicks;

    public void toggle() {
        if (enabled) {
            stop();
        } else {
            intervalTicks = DEFAULT_INTERVAL_TICKS;
            elapsedTicks = 0;
            enabled = true;
        }
    }

    public void start(int ticks) {
        if (ticks < 1) {
            throw new IllegalArgumentException("Interval must be a positive number of ticks");
        }
        intervalTicks = ticks;
        elapsedTicks = 0;
        enabled = true;
    }

    public void stop() {
        enabled = false;
        intervalTicks = DEFAULT_INTERVAL_TICKS;
        elapsedTicks = 0;
    }

    /** Returns true exactly once per interval of active client ticks. */
    public boolean tick() {
        if (!enabled) {
            return false;
        }
        if (++elapsedTicks >= intervalTicks) {
            elapsedTicks = 0;
            return true;
        }
        return false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int intervalTicks() {
        return intervalTicks;
    }

}
