package com.zipdb.observability;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsCollector {

    private final AtomicLong commandCount = new AtomicLong();

    public void incrementCommandCount() {
        commandCount.incrementAndGet();
    }

    public long getCommandCount() {
        return commandCount.get();
    }

    public long getHeapMemoryUsed() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        return heap.getUsed();
    }

    public String getMetricsSnapshot() {
        return "Commands Processed: " + getCommandCount() + ", Heap Memory Used: " + getHeapMemoryUsed() / 1024 + " KB";
    }
}
