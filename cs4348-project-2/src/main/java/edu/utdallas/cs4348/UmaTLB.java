/*
* Author: Uma Rao
* Class: CS 4348.002, Project 2 (Simulate a Memory System)
*/
package edu.utdallas.cs4348;

public class UmaTLB extends TLB {
    // Constructs a UmaTLB and logs its creation.
    public UmaTLB() {
        super(); // Implicit call to the superclass constructor.
        log("TLB Constructor called.");
    }

    // Adds a page table entry to the TLB, replacing the LRU entry if necessary.
    @Override
    public void addEntry(PageTableEntry entry) {
        // First check for duplicate entries.
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] != null && entries[i].getProcessID() == entry.getProcessID() && entries[i].getPageNumber() == entry.getPageNumber()) {
                log("Duplicate entry detected for process " + entry.getProcessID() + ", page " + entry.getPageNumber() + ". Skipping add.");
                return;
            }
        }

        // Attempt to add the entry to the first available spot.
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] == null) {
                entries[i] = entry;
                entry.access(); // Update access time for the entry.
                log("Added new entry for process " + entry.getProcessID() + ", page " + entry.getPageNumber() + ", at index " + i);
                return;
            }
        }

        // If all spots are taken, find and replace the least recently used entry.
        int lruIndex = 0;
        long lruTime = entries[0].getLastAccessed();
        for (int i = 1; i < entries.length; i++) {
            if (entries[i].getLastAccessed() < lruTime) {
                lruIndex = i;
                lruTime = entries[i].getLastAccessed();
            }
        }

        // Replace the LRU entry with the new entry.
        entries[lruIndex] = entry;
        entry.access(); // Update access time for the entry.
        log("Replaced LRU entry for process " + entry.getProcessID() + ", page " + entry.getPageNumber() + ", at index " + lruIndex);
    }

    // Looks up a page number for a given process, updating its access time if found.
    @Override
    public int lookup(int pageNumber, Process process) {
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] != null && entries[i].getProcessID() == process.getProcessID() && entries[i].getPageNumber() == pageNumber) {
                entries[i].access(); // Update access time.
                log("TLB hit for process " + process.getProcessID() + ", page " + pageNumber + ", at index " + i);
                return entries[i].getFrameNumber();
            }
        }
        log("TLB miss for process " + process.getProcessID() + ", page " + pageNumber);
        return -1; // Indicate miss.
    }

   // Logs messages, prepending them with the calling test class name.
    private void log(String message) {
        System.out.println(findJUnitTestClass() + ": " + message);
    }

    // Finds the first test class name in the stack trace that contains "Test".
    private String findJUnitTestClass() {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            if (ste.getClassName().contains("Test")) {
                return ste.getClassName().substring(ste.getClassName().lastIndexOf('.') + 1);
            }
        }
        return "Unknown Test Class";
    }
}
