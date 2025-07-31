package com.genius.primavera.lightweight.framework.testcomponents;

import com.genius.primavera.lightweight.annotations.PrimaveraAutowired;
import com.genius.primavera.lightweight.annotations.PrimaveraComponent;

@PrimaveraComponent
public class TestController {
    @PrimaveraAutowired
    private TestService testService;
    
    public TestService getTestService() {
        return testService;
    }
    
    public String processRequest() {
        return testService.getMessage();
    }
}