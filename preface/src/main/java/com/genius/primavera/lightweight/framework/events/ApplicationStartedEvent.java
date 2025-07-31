package com.genius.primavera.lightweight.framework.events;

/**
 * 애플리케이션이 시작되었을 때 발생하는 이벤트
 */
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