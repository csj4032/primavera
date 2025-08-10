package com.genius.primavera.lightweight.framework.events;

import java.time.LocalDateTime;

public abstract class PrimaveraApplicationEvent {
    
    private final Object source;
    private final LocalDateTime timestamp;
    
    protected PrimaveraApplicationEvent(Object source) {
        this.source = source;
        this.timestamp = LocalDateTime.now();
    }
    
    public Object getSource() {
        return source;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[source=" + source + ", timestamp=" + timestamp + "]";
    }
}