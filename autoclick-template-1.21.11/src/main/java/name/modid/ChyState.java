package name.modid;

/** Session state; reset on disconnect so no automated input leaks into another world. */
public final class ChyState {
    public final ClickState leftClick = new ClickState();
    public final ClickState rightClick = new ClickState();
    private boolean sneaking;

    public void toggleSneaking() {
        sneaking = !sneaking;
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public void reset() {
        leftClick.stop();
        rightClick.stop();
        sneaking = false;
    }
}
