package com.genius.primavera.testcontainer.v4;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("ContainerRegistry Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContainerRegistryTest {
    
    @Test
    @Order(1)
    @DisplayName("Registry starts with no manager")
    void testRegistryStartsEmpty() {
        ContainerRegistry.clear();
        
        ContainerManager manager = ContainerRegistry.get();
        assertNull(manager, "Registry should start with no manager");
        
        log.info("✅ Registry starts empty");
    }
    
    @Test
    @Order(2)
    @DisplayName("Can register and retrieve manager")
    void testRegisterAndRetrieve() {
        ContainerRegistry.clear();
        
        EnableTestContainers annotation = createMockAnnotation();
        ContainerManager mockManager = new ContainerManager(annotation, this.getClass());
        
        ContainerRegistry.register(mockManager);
        
        ContainerManager retrieved = ContainerRegistry.get();
        assertSame(mockManager, retrieved, "Should retrieve the same manager instance");
        
        log.info("✅ Manager registered and retrieved successfully");
    }
    
    @Test
    @Order(3)
    @DisplayName("Can clear registry")
    void testClearRegistry() {
        EnableTestContainers annotation = createMockAnnotation();
        ContainerManager mockManager = new ContainerManager(annotation, this.getClass());
        
        ContainerRegistry.register(mockManager);
        assertNotNull(ContainerRegistry.get(), "Manager should be present before clear");
        
        ContainerRegistry.clear();
        assertNull(ContainerRegistry.get(), "Manager should be null after clear");
        
        log.info("✅ Registry cleared successfully");
    }
    
    @Test
    @Order(4)
    @DisplayName("Thread local isolation works")
    void testThreadLocalIsolation() throws InterruptedException {
        ContainerRegistry.clear();
        
        EnableTestContainers annotation1 = createMockAnnotation();
        ContainerManager manager1 = new ContainerManager(annotation1, this.getClass());
        
        ContainerRegistry.register(manager1);
        
        final ContainerManager[] otherThreadManager = new ContainerManager[1];
        
        Thread otherThread = new Thread(() -> {
            otherThreadManager[0] = ContainerRegistry.get();
        });
        
        otherThread.start();
        otherThread.join();
        
        assertSame(manager1, ContainerRegistry.get(), "Main thread should have its manager");
        assertNull(otherThreadManager[0], "Other thread should not see main thread's manager");
        
        log.info("✅ Thread local isolation verified");
    }
    
    @Test
    @Order(5)
    @DisplayName("Can get lock for class")
    void testGetLock() {
        String className = "TestClass";
        
        Object lock1 = ContainerRegistry.getLock(className);
        Object lock2 = ContainerRegistry.getLock(className);
        
        assertNotNull(lock1, "Lock should not be null");
        assertSame(lock1, lock2, "Same class should get same lock instance");
        
        Object differentLock = ContainerRegistry.getLock("DifferentClass");
        assertNotSame(lock1, differentLock, "Different classes should get different locks");
        
        log.info("✅ Class locks work correctly");
    }
    
    @Test
    @Order(6)
    @DisplayName("Can remove lock for class")
    void testRemoveLock() {
        String className = "RemovableTestClass";
        
        Object lock1 = ContainerRegistry.getLock(className);
        assertNotNull(lock1, "Lock should be created");
        
        ContainerRegistry.removeLock(className);
        
        Object lock2 = ContainerRegistry.getLock(className);
        assertNotNull(lock2, "New lock should be created after removal");
        assertNotSame(lock1, lock2, "New lock should be different instance");
        
        log.info("✅ Lock removal works correctly");
    }
    
    @Test
    @Order(7)
    @DisplayName("Multiple lock operations are thread safe")
    void testLockThreadSafety() throws InterruptedException {
        String className = "ThreadSafeTestClass";
        
        final Object[] locks = new Object[10];
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                locks[index] = ContainerRegistry.getLock(className);
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        Object firstLock = locks[0];
        for (int i = 1; i < locks.length; i++) {
            assertSame(firstLock, locks[i], 
                "All threads should get the same lock instance for same class");
        }
        
        log.info("✅ Lock thread safety verified");
    }
    
    private EnableTestContainers createMockAnnotation() {
        return new EnableTestContainers() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return EnableTestContainers.class;
            }
            
            @Override
            public TestContainer[] value() {
                return new TestContainer[]{
                    new TestContainer() {
                        @Override
                        public Class<? extends java.lang.annotation.Annotation> annotationType() {
                            return TestContainer.class;
                        }
                        
                        @Override
                        public ContainerType type() {
                            return ContainerType.MARIADB;
                        }
                        
                        @Override
                        public String name() {
                            return "testContainer";
                        }
                    }
                };
            }
        };
    }
}