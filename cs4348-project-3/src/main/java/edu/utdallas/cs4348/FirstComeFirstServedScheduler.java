package edu.utdallas.cs4348;

import java.util.LinkedList;
import java.util.Queue;

public class FirstComeFirstServedScheduler implements CPUScheduler {

    private final Queue<CPUBurst> burstQueue = new LinkedList<>(); //Queue to hold bursts
    private CPUBurst activeBurst = null; //Current active burst

    @Override
    public void addTask(CPUBurst burst) {
        // TODO: fill in
        if (activeBurst == null) {
            activeBurst = burst;
            System.out.println("    Starting new task");
            burst.start(this);
        } else {
            burstQueue.add(burst);
            System.out.println("    Added task to queue");
        }
    }

    @Override
    public void done(CPUBurst burst) {
        // TODO: fill in
        if (burst != null) {
            System.out.println("    Task completed");
        }

        activeBurst = burstQueue.poll();
        if (activeBurst != null) {
            System.out.println("    Starting next task from queue");
            activeBurst.start(this);
        } else {
            System.out.println("    No tasks remaining");
        }
    }

    @Override
    public boolean hasTasks() {
        // TODO: fill in
        return activeBurst != null || !burstQueue.isEmpty();
    }
}