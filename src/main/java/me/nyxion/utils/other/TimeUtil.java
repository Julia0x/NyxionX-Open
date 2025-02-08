package me.nyxion.utils.other;

public class TimeUtil {
    private long currentMs;

    public TimeUtil() {
        reset();
    }

    public boolean hasReached(int milliseconds) {
        return elapsed() >= milliseconds;
    }

    public boolean hasReached(long milliseconds) {
        return elapsed() >= milliseconds;
    }

    public void resetWithOffset(long offset) {
        this.currentMs = getTime() + offset;
    }

    public long elapsed() {
        return System.currentTimeMillis() - currentMs;
    }

    public void reset() {
        currentMs = System.currentTimeMillis();
    }

    public boolean isDelayComplete(float delay) {
        return (float)(System.currentTimeMillis() - this.currentMs) > delay;
    }

    private long getTime() {
        return System.nanoTime() / 1000000L;
    }
}