package name.modid;

/** Tick-based timing shared by the client commands and attack loop. */
public final class AutoAttackState {
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

    public void start(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0.01 || seconds > 10.0) {
            throw new IllegalArgumentException("Interval must be between 0.01 and 10 seconds");
        }
        // Decimal arithmetic avoids turning exact decimal tick boundaries into an extra tick.
        intervalTicks = java.math.BigDecimal.valueOf(seconds)
            .multiply(java.math.BigDecimal.valueOf(20))
            .setScale(0, java.math.RoundingMode.CEILING).intValueExact();
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

    public double intervalSeconds() {
        return intervalTicks / 20.0;
    }
}
