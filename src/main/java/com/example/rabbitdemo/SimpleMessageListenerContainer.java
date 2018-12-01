package com.example.rabbitdemo;

import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public class SimpleMessageListenerContainer extends AbstractMessageListenerContainer
        implements ApplicationEventPublisherAware {
    // 并发消费者数量默认为1
    private volatile int concurrentConsumers = 1;

    public void setConcurrentConsumers(final int concurrentConsumers) {
        // 动态增加或消费消费者，do other operation
    }

    @Override
    protected void doInitialize() throws Exception {

    }

    @Override
    protected void doShutdown() {

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {

    }

    // ......
}