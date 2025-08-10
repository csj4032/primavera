package com.genius.primavera.lightweight.framework.events;

public class ApplicationStartedEvent extends PrimaveraApplicationEvent {
    
    private final long startupTime;
    
    public ApplicationStartedEvent(Object source, long startupTime) {
        super(source);
        this.startupTime = startupTime;
    }
    
    public long getStartupTime() {
        return startupTime;
    }
}