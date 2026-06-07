package com.gregtechceu.gtceu.api.machine;

public final class TickTask implements Runnable {

    private final int priority;
    private final long tick;
    private final Runnable runnable;

    public static TickTask of(Runnable runnable) {
        return new TickTask(0, 0, runnable);
    }

    public static TickTask withPriority(int priority, Runnable runnable) {
        return new TickTask(priority, 0, runnable);
    }

    public TickTask(int priority, long tick, Runnable runnable) {
        this.priority = priority;
        this.tick = tick;
        this.runnable = runnable;
    }

    public int priority() {
        return priority;
    }

    public long tick() {
        return tick;
    }

    public boolean canRun(long currentTick) {
        return currentTick == tick;
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}
