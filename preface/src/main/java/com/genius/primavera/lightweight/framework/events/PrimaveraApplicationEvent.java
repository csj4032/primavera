package com.genius.primavera.lightweight.framework.events;

import java.time.LocalDateTime;

/**
 * Primavera 애플리케이션에서 발생하는 이벤트의 기본 클래스
 * Spring의 ApplicationEvent와 유사한 역할을 합니다.
 */
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