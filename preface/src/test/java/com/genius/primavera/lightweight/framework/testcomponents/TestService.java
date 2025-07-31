package com.genius.primavera.lightweight.framework.testcomponents;

import com.genius.primavera.lightweight.annotations.PrimaveraComponent;

@PrimaveraComponent
public class TestService {
    public String getMessage() {
        return "Test Message";
    }
}