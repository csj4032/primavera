package com.genius.primavera.lightweight.framework.lifecycle;

import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.annotations.PrimaveraPostConstruct;
import com.genius.primavera.lightweight.annotations.PrimaveraPreDestroy;

@PrimaveraComponent
public class TestLifecycleBean {
    
    private boolean initialized = false;
    private boolean destroyed = false;
    private String message;
    private String cleanupMessage;
    
    @PrimaveraPostConstruct
    public void initialize() {
        this.initialized = true;
        this.message = "초기화 완료!";
    }
    
    @PrimaveraPreDestroy
    public void cleanup() {
        this.destroyed = true;
        this.cleanupMessage = "정리 완료!";
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public boolean isDestroyed() {
        return destroyed;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getCleanupMessage() {
        return cleanupMessage;
    }
}