package com.genius.primavera.lightweight.framework.events;

/**
 * Bean이 생성되었을 때 발생하는 이벤트
 */
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