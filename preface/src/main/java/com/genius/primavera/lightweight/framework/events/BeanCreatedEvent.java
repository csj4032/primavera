package com.genius.primavera.lightweight.framework.events;

public class BeanCreatedEvent extends PrimaveraApplicationEvent {
    
    private final String beanName;
    private final Object bean;
    
    public BeanCreatedEvent(Object source, String beanName, Object bean) {
        super(source);
        this.beanName = beanName;
        this.bean = bean;
    }
    
    public String getBeanName() {
        return beanName;
    }
    
    public Object getBean() {
        return bean;
    }
}