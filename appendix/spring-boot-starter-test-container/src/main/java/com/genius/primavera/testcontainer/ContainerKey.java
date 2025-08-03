package com.genius.primavera.testContainer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ContainerKey {

    private final ContainerType containerType;
    private final ContainerLifecycleMode lifecycleMode;
    private final String testClassName;

    private ContainerKey(ContainerType containerType, ContainerLifecycleMode lifecycleMode, String testClassName) {
        this.containerType = containerType;
        this.lifecycleMode = lifecycleMode;
        this.testClassName = testClassName;
    }

    public static ContainerKey forReuse(ContainerType containerType) {
        return new ContainerKey(containerType, ContainerLifecycleMode.REUSE, "GLOBAL");
    }

    public static ContainerKey forPerClass(ContainerType containerType, String testClassName) {
        return new ContainerKey(containerType, ContainerLifecycleMode.PER_CLASS, testClassName);
    }

    public static ContainerKey forPerTest(ContainerType containerType) {
        return new ContainerKey(containerType, ContainerLifecycleMode.PER_TEST,
                "THREAD-" + Thread.currentThread().getId());
    }

    public static ContainerKey create(ContainerType containerType, ContainerLifecycleMode lifecycleMode, String testClassName) {
        return switch (lifecycleMode) {
            case REUSE -> forReuse(containerType);
            case PER_CLASS -> forPerClass(containerType, testClassName);
            case PER_TEST -> forPerTest(containerType);
        };
    }

    public String getDisplayName() {
        return switch (lifecycleMode) {
            case REUSE -> containerType + "(GLOBAL)";
            case PER_CLASS -> containerType + "(" + getSimpleClassName() + ")";
            case PER_TEST -> containerType + "(" + testClassName + ")";
        };
    }

    private String getSimpleClassName() {
        if (testClassName == null || testClassName.isEmpty()) {
            return "UNKNOWN";
        }
        return testClassName.substring(testClassName.lastIndexOf('.') + 1);
    }
}