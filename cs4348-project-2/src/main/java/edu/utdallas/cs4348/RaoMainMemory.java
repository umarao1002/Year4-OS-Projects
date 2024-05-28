/*
 * Author: Uma Rao
 * Class: CS 4348.002, Project 2 (Simulate a Memory System)
 */
package edu.utdallas.cs4348;

public class RaoMainMemory extends MainMemory {
    private final int[] additionOrder;
    private int orderCounter = 0;

    // Constructor for RaoMainMemory.
    public RaoMainMemory(int numFrames, TLB tlb) {
        super(numFrames, tlb);
        additionOrder = new int[numFrames];
        for (int i = 0; i < numFrames; i++) {
            additionOrder[i] = -1; // Indicate that no page is initially added.
        }
    }

    // Gets the physical address corresponding to a logical address.
    @Override
    public void getPhysicalAddress(LookupInfo lookupInfo) {
        int pageNumber = lookupInfo.getLogicalAddress() >> Util.NUM_BITS_WITHIN_FRAME;
        int frameNumber = -1;

        if (tlb != null) {
            frameNumber = tlb.lookup(pageNumber, lookupInfo.getProcess());
        }

        if (frameNumber != -1) {
            lookupInfo.setTlbHit(true);
        } else {
            PageTableEntry entry = lookupInfo.getProcess().getEntryAt(pageNumber);
            if (entry != null && entry.getFrameNumber() != -1) {
                frameNumber = entry.getFrameNumber();
                if (tlb != null) tlb.addEntry(entry);
            }
        }

        if (frameNumber != -1) {
            int physicalAddress = frameNumber * Util.SIZE_OF_FRAME +
                    (lookupInfo.getLogicalAddress() & Util.LOCATION_WITHIN_PAGE_OR_FRAME_MASK);
            lookupInfo.setPhysicalAddress(physicalAddress);
            log("Mapped logical address " + lookupInfo.getLogicalAddress() + " to physical address " + physicalAddress);
        } else {
            lookupInfo.setPhysicalAddress(-1);
            log("Failed to map logical address " + lookupInfo.getLogicalAddress() + " to a physical address.");
        }
    }

    // Adds a page to memory.
    @Override
    public void addPageToMemory(PageTableEntry pageTableEntry) {
        boolean pageAdded = false;
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].isEmpty()) {
                frames[i].assignFrame(pageTableEntry);
                additionOrder[i] = ++orderCounter;
                pageAdded = true;
                log("Added page " + pageTableEntry.getPageNumber() + " to frame " + i);
                break;
            }
        }

        if (!pageAdded) {
            int evictionIndex = findEvictionIndex();
            frames[evictionIndex].clearFrame();
            frames[evictionIndex].assignFrame(pageTableEntry);
            additionOrder[evictionIndex] = ++orderCounter;
            log("Replaced page in frame " + evictionIndex + " with new page " + pageTableEntry.getPageNumber());
        }
    }

    // Finds the index of the frame to evict based on the addition order.
    private int findEvictionIndex() {
        int oldestIndex = 0;
        int oldestOrder = additionOrder[0];
        for (int i = 1; i < additionOrder.length; i++) {
            if (additionOrder[i] < oldestOrder) {
                oldestIndex = i;
                oldestOrder = additionOrder[i];
            }
        }
        return oldestIndex;
    }

    // Logs a message with the name of the test class that invoked the method.
    private void log(String message) {
        System.out.println(findJUnitTestClass() + ": " + message);
    }

    // Finds the name of the JUnit test class in the stack trace.
    private String findJUnitTestClass() {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            if (ste.getClassName().contains("Test")) {
                return ste.getClassName().substring(ste.getClassName().lastIndexOf('.') + 1);
            }
        }
        return "Unknown Test Class";
    }
}


