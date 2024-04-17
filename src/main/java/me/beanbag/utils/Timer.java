package me.beanbag.utils;

public class Timer {
    private long time;
    public Timer() {
        time = System.currentTimeMillis();
    }
    public long getPassedTimeMs() {
        return getMs(System.nanoTime() - time);
    }
    public long getMs(long time) {
        return time / 1000000L;
    }
    public boolean passedNS(long ns) {
        return System.nanoTime() - time >= ns;
    }
    public long convertToNS(long time) {
        return time * 1000000L;
    }
    public Timer reset() {
        time = System.nanoTime();
        return this;
    }
}
