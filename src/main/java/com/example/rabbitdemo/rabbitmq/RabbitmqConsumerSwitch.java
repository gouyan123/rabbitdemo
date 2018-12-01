package com.example.rabbitdemo.rabbitmq;

import org.springframework.amqp.rabbit.listener.AsyncConsumerStartedEvent;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Collection;

@Component
@ConfigurationProperties(prefix = "rabbitmq.consumer")
public class RabbitmqConsumerSwitch implements InitializingBean{

    private RabbitListenerEndpointRegistry registry;
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    @Autowired
    public void registry(RabbitListenerEndpointRegistry registry){
        this.registry = registry;
    }
    @EventListener
    public void onAsyncConsumerStartedEvent(AsyncConsumerStartedEvent event){
        if (state.equalsIgnoreCase("off")) {
            Collection<MessageListenerContainer> containers = this.registry.getListenerContainers();
            for (MessageListenerContainer container : containers){
                container.stop();
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (state.equalsIgnoreCase("on")) {
        Collection<MessageListenerContainer> containers = this.registry.getListenerContainers();
            for (MessageListenerContainer container : containers){
                container.start();
            }
        }
    }

}
