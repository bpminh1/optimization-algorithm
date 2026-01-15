package org.example.model.core;

/**
 * Listener interface for receiving iteration updates during an optimization process.
 */
public interface IterationListener {
    /**
     * Called when a new iteration is completed.
     *
     * @param info Information about the completed iteration.
     */
    void onIteration(IterationInfo info);
}
