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
        this.message = "translated_text_3 completed!";
    }
    
    @PrimaveraPreDestroy
    public void cleanup() {
        this.destroyed = true;
        this.cleanupMessage = "translated_text_2 completed!";
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