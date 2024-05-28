package edu.utdallas.cs4348;

import java.util.LinkedList;
import java.util.List;

public class RRScheduler implements CPUScheduler {

    private final List<CPUBurst> queue;
    private CPUBurst currentTask;
    private Timer timer;
    private final int timeQuantum;

    public RRScheduler(int timeQuantum) {
        // Initialize the time quantum, queue, and current task
        this.timeQuantum = timeQuantum;
        this.queue = new LinkedList<>();
        this.currentTask = null;
    }

    @Override
    public void addTask(CPUBurst burst) {
        // TODO: fill in
        // If there is no current task, start the new task
        // Otherwise, add the new task to the queue
        if (currentTask == null) {
            currentTask = burst;
            System.out.println("   Starting new task with time quantum: " + timeQuantum);
            burst.start(this);
            setupTimer();
        } else {
            queue.add(burst);
            System.out.println("   Added task to queue, waiting for time quantum expiration.");
        }
    }

    //clears any existing timer and sets up a new timer with a specified time quantum;
    //when the timer expires, it stops the current task, potentially requeues it, and
    //starts the next task if available.
    private void setupTimer() {
        // Clear previous timer, sets new timer with time quantum
        if (timer != null) {
            TimedTasks.clearTimer();
            System.out.println("   Clearing existing timer.");
        }
        timer = new Timer(timeQuantum) {
            @Override
            void timerExpired() {
                // stop current task, add to queue end and start the next task from queue
                System.out.println("   Timer expired, preempting current task.");
                if (currentTask != null) {
                    currentTask.stop();
                    System.out.println("   Stopped task due to timer expiration.");
                    queue.add(currentTask);
                }
                if (!queue.isEmpty()) {
                    currentTask = queue.remove(0);
                    System.out.println("   Starting next task from queue.");
                    currentTask.start(RRScheduler.this);
                    setupTimer(); // Re-setup timer for the new task
                } else {
                    System.out.println("   No tasks left in queue.");
                }
            }
        };
        TimedTasks.addTimer(timer);
        System.out.println("   Timer set for task with quantum of " + timeQuantum);
    }

    @Override
    public void done(CPUBurst burst) {
        // TODO: fill in
        // Clear the timer, start next task and set up new time
        // set the current task to null, if no next tasks exists
        TimedTasks.clearTimer();
        System.out.println("   Task completed normally.");
        if (!queue.isEmpty()) {
            currentTask = queue.remove(0);
            System.out.println("   Starting next queued task after completion.");
            currentTask.start(this);
            setupTimer();
        } else {
            currentTask = null;
            System.out.println("   No further tasks to run.");
        }
    }

    @Override
    public boolean hasTasks() {
        // TODO: fill in
        // Return true if there is a current task or if the queue is not empty
        boolean hasTasks = currentTask != null || !queue.isEmpty();
        System.out.println("   Checking task status: " + (hasTasks ? "Tasks present" : "No tasks present"));
        return hasTasks;
    }
}
