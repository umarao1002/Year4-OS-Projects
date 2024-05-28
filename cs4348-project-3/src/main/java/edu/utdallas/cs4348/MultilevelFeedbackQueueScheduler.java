package edu.utdallas.cs4348;

import java.util.LinkedList;
import java.util.List;

public class MultilevelFeedbackQueueScheduler implements CPUScheduler {

    private static final int FIRST_QUEUE_QUANTUM = 3;
    private static final int SECOND_QUEUE_QUANTUM = 6;
    private static final int NO_QUANTUM = 99; // Used for last queue which has no timer.

    private final List<CPUBurst> firstQueue = new LinkedList<>();
    private final List<CPUBurst> secondQueue = new LinkedList<>();
    private final List<CPUBurst> lastQueue = new LinkedList<>();
    private CPUBurst activeBurst = null;
    private Timer timer;

    @Override
    public void addTask(CPUBurst burst) {
        // TODO: fill in
        if (activeBurst == null) {
            activeBurst = burst;
            System.out.println("    Starting initial task.");
            burst.start(this);
            setupTimer(FIRST_QUEUE_QUANTUM);
        } else {
            firstQueue.add(burst);
            System.out.println("    Added task to first queue.");
        }
    }

    @Override
    public void done(CPUBurst burst) {
        // TODO: fill in
        // Print the completed task
        if (burst != null) {
            System.out.println("    Task completed.");
        }

        if (!firstQueue.isEmpty()) {
            activeBurst = firstQueue.remove(0);
            System.out.println("    Starting task from first queue.");
            setupTimer(FIRST_QUEUE_QUANTUM);
        } else if (!secondQueue.isEmpty()) {
            activeBurst = secondQueue.remove(0);
            System.out.println("    Starting task from second queue.");
            setupTimer(SECOND_QUEUE_QUANTUM);
        } else if (!lastQueue.isEmpty()) {
            activeBurst = lastQueue.remove(0);
            System.out.println("    Starting task from last queue.");
            setupTimer(NO_QUANTUM); // No timer for the last queue
        } else {
            activeBurst = null;
            System.out.println("    No remaining tasks.");
        }

        // Start the new active task
        if (activeBurst != null) {
            activeBurst.start(this);
        }
    }

    @Override
    public boolean hasTasks() {
        // TODO: fill in
        // Check if there are any tasks in the queues or an active task
        return activeBurst != null || !firstQueue.isEmpty() || !secondQueue.isEmpty() || !lastQueue.isEmpty();
    }

    private void setupTimer(int quantum) {
        // Clear any existing timer
        if (timer != null) {
            TimedTasks.clearTimer();
        }

        // Create a new timer with the specified quantum
        timer = new Timer(quantum) {
            @Override
            void timerExpired() {
                // If there is an active task, preempt it and move it to the appropriate queue
                if (activeBurst != null) {
                    activeBurst.stop();
                    System.out.println("    Timer expired, preempting current task.");
                    if (quantum == FIRST_QUEUE_QUANTUM) {
                        secondQueue.add(activeBurst);
                        System.out.println("    Moved from first to second queue.");
                    } else if (quantum == SECOND_QUEUE_QUANTUM) {
                        lastQueue.add(activeBurst);
                        System.out.println("    Moved from second to last queue.");
                    }
                    activeBurst = null;
                }
                // Call the done method to schedule the next task
                done(null);
            }
        };
        // Add the new timer to the TimedTasks instance
        TimedTasks.addTimer(timer);
        System.out.println("    Timer set for task with quantum of " + quantum);
    }
}