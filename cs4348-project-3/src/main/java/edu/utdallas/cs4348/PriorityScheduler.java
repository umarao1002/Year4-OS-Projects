package edu.utdallas.cs4348;

import java.util.*;

public class PriorityScheduler implements CPUScheduler {

    private final TreeMap<Integer, LinkedList<CPUBurst>> tasks = new TreeMap<>(Collections.reverseOrder());
    private CPUBurst activeBurst = null;

    @Override
    public void addTask(CPUBurst burst) {
        // TODO: fill in
        if (activeBurst == null) {
            activeBurst = burst;
            System.out.println("    Starting task with priority " + burst.getPriority());
            burst.start(this); // Start burst if no active tasks
        } else {
            if (burst.getPriority() > activeBurst.getPriority()) {
                System.out.println("    Preempting task with priority " + activeBurst.getPriority());
                activeBurst.stop(); // Preempt current task
                // Place the current task back at the front of its queue
                tasks.computeIfAbsent(activeBurst.getPriority(), k -> new LinkedList<>()).addFirst(activeBurst);
                activeBurst = burst;
                System.out.println("    Starting higher priority task with priority " + burst.getPriority());
                burst.start(this); // Start new higher priority task
            } else {
                // Queue the burst at its priority level
                tasks.computeIfAbsent(burst.getPriority(), k -> new LinkedList<>()).offer(burst);
                System.out.println("    Added task with priority " + burst.getPriority() + " to queue");
            }
        }
    }

    @Override
    public void done(CPUBurst burst) {
        // TODO: fill in
        if (burst != null) {
            System.out.println("    Completed task with priority " + burst.getPriority());
        }

        LinkedList<CPUBurst> queue = tasks.getOrDefault(burst.getPriority(), new LinkedList<>());
        if (!queue.isEmpty()) {
            activeBurst = queue.poll(); //continue with next tasks of same priority
            System.out.println("    Starting task with priority " + activeBurst.getPriority() + " from same priority queue");
            activeBurst.start(this);
        } else {
            // Find the next highest priority task
            activeBurst = null;
            for (var entry : tasks.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    activeBurst = entry.getValue().poll();
                    if (activeBurst != null) {
                        System.out.println("    Starting task with priority " + activeBurst.getPriority() + " from higher priority queue");
                        activeBurst.start(this);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean hasTasks() {
        // TODO: fill in
        if (activeBurst != null) {
            return true;
        }
        // Check all remaining tasks
        for (Queue<CPUBurst> queue : tasks.values()) {
            if (!queue.isEmpty()) {
                return true;
            }
        }
        System.out.println("    No tasks remaining");
        return false;
    }
}