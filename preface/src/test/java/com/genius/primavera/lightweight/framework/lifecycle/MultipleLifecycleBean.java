package com.genius.primavera.lightweight.framework.lifecycle;

import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.annotations.PrimaveraPostConstruct;

@PrimaveraComponent
public class MultipleLifecycleBean {
    
    private boolean firstInitCalled = false;
    private boolean secondInitCalled = false;
    
    @PrimaveraPostConstruct
    public void firstInit() {
        this.firstInitCalled = true;
    }
    
    @PrimaveraPostConstruct
    public void secondInit() {
        this.secondInitCalled = true;
    }
    
    public boolean isFirstInitCalled() {
        return firstInitCalled;
    }
    
    public boolean isSecondInitCalled() {
        return secondInitCalled;
    }
}