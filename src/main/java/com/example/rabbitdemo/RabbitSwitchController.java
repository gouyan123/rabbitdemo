package com.shanglv51.domesticticketorderservice.controller;

import lombok.Data;
import org.springframework.amqp.rabbit.listener.AsyncConsumerStartedEvent;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class RabbitSwitchController implements ApplicationContextAware {
    private AtomicInteger count = new AtomicInteger(0);

    private ApplicationContext context;

    private volatile boolean off = true;

    @Autowired
    private RabbitListenerEndpointRegistry registry;

    @EventListener
    public void onAsyncConsumerStartedEvent(AsyncConsumerStartedEvent event){
        if (off == false) return;
        registry.stop();
    }

    private Map<String,SimpleMessageListenerContainer> queueMap = new HashMap<String,SimpleMessageListenerContainer>();

//    private Map<String, List<String>> stateMap = new HashMap<>();
    private HashMap<String, ListenerInstance> stateMap = new HashMap<String,ListenerInstance>();

    @RequestMapping(value = "/getAll",method = RequestMethod.GET)
    public HashMap<String, ListenerInstance> getAll(){
        updateMap();
        return stateMap;
    }
    @RequestMapping(value = "/stopAll",method = RequestMethod.GET)
    public void stopAll(){
        registry.stop();
    }
    @RequestMapping(value = "/startAll",method = RequestMethod.GET)
    public void startAll(){
        off = false;
        registry.start();
    }
    @RequestMapping(value = "/stopSome",method = RequestMethod.GET)
    public void stopSome(String queueName){
        SimpleMessageListenerContainer container = queueMap.get(queueName);
        if (container != null){
            container.stop();
        }
    }
    @RequestMapping(value = "/startSome",method = RequestMethod.GET)
    public void startSome(String queueName){
        off = false;
        SimpleMessageListenerContainer container = queueMap.get(queueName);
        if (container != null){
            container.start();
        }
    }
    public void updateMap(){
        Collection<MessageListenerContainer> containers = registry.getListenerContainers();
        SimpleMessageListenerContainer simpleContainer = null;
        for (MessageListenerContainer container : containers) {
            simpleContainer = (SimpleMessageListenerContainer) container;
            String[] queueNames = simpleContainer.getQueueNames();
            ListenerInstance listenerInstance = new ListenerInstance();
            listenerInstance.setListenerId(simpleContainer.getListenerId());
            listenerInstance.setIsRunning(simpleContainer.isRunning());
            listenerInstance.setQueueNames(Arrays.asList(queueNames));
            stateMap.put("ListenerId : " + simpleContainer.getListenerId(), listenerInstance);

//            if (count.incrementAndGet() > 1) continue;
            for (String name : queueNames){
                queueMap.put(name,simpleContainer);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Data
    static class ListenerInstance{
        private String listenerId;
        private Boolean isRunning;
        private List<String> queueNames = new ArrayList<>();
    }

}
