package com.gregtechceu.gtceu.api.machine;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import lombok.Getter;

import java.util.Comparator;

public class TickTaskHandler {

    @Getter
    private final boolean client;
    @Getter
    private long tickCount;
    private final ObjectSortedSet<TickTask> tickableSubscriptions = new ObjectAVLTreeSet<>(
            Comparator.comparingInt(TickTask::priority));

    public TickTaskHandler(boolean client) {
        this.client = client;
    }

    public void tick() {
        ObjectBidirectionalIterator<TickTask> iterator = tickableSubscriptions.iterator();
        while (iterator.hasNext()) {
            TickTask task = iterator.next();
            if (task.canRun(this.tickCount)) {
                task.run();
                iterator.remove();
            }
        }
        tickCount++;
    }

    public TickTask delayedTask(int delay, Runnable runnable) {
        return delayedTask(delay, 0, runnable);
    }

    public TickTask delayedTask(int delay, int priority, Runnable runnable) {
        return new TickTask(priority, this.tickCount + delay, runnable);
    }

    public void clientTask(TickTask task) {
        if (client) addTask(task);
    }

    public void serverTask(TickTask task) {
        if (!client) addTask(task);
    }

    public void addTask(TickTask task) {
        this.tickableSubscriptions.add(task);
    }

    public void clear() {
        tickableSubscriptions.clear();
    }
}
