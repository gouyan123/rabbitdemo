package com.example.rabbitdemo.rabbitmq;

import org.springframework.amqp.rabbit.listener.AsyncConsumerStartedEvent;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * rabbitmq消费者开关
 * */
@Component
@ConfigurationProperties(prefix = "rabbitmq.consumer")
public class RabbitmqConsumerSwitch implements InitializingBean,ApplicationContextAware {

    private Param param;
    @Autowired
    @RefreshScope
    public void setParam(Param param) {
        this.param = param;
    }

    private ApplicationContext context;
    private RabbitListenerEndpointRegistry registry;
    /**key：要关闭的队列名称
     * value：要关闭的队列所在的服务实例名称*/
    private List<Map<String,String>> closeList = new ArrayList<>();
    /**nodeClosedSet里面存服务实例名称，这些实例里面需要关闭rabbitmq自动消费队列*/
    private Set<String> nodeClosedSet = new HashSet<String>();
    /**queueClosedSet里面存队列名称，这些队列将不会被rabbitmq消费者消费*/
    private Set<String> queueClosedSet = new HashSet<String>();
    private Environment environment;
//    private DiscoveryClient discoveryClient;
    /**当前服务实例名称，在bootstray.yml中定义，例如 service.instance=node1*/
    private String currentNode;

    public RabbitListenerEndpointRegistry getRegistry(){
        return this.registry;
    }
    @EventListener
    public void onAsyncConsumerStartedEvent(AsyncConsumerStartedEvent event){
        if (nodeClosedSet.contains(currentNode)) {
            stopContainers(queueClosedSet);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        nodeClosedSet.clear();
        queueClosedSet.clear();
        this.closeList = param.getCloseList();
        if (this.closeList!=null && this.closeList.size()>0){
            for (Map<String,String> map : closeList){
                for (String key : map.keySet()){
                    this.nodeClosedSet.add(map.get(key));
                    if (map.get(key).equals(currentNode)){
                        queueClosedSet.add(key);
                    }
                }
            }
        }
        if (queueClosedSet != null && queueClosedSet.size() > 0){
            startContainers(queueClosedSet);
        }

        if (nodeClosedSet.contains(currentNode)) {
            stopContainers(queueClosedSet);
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        this.registry = this.context.getBean(RabbitListenerEndpointRegistry.class);
        this.environment = this.context.getEnvironment();
        currentNode = environment.getProperty("service.instance");
    }

    public List<Map<String, String>> getCloseList() {
        return closeList;
    }

    public void setCloseList(List<Map<String, String>> closeList) {
        this.closeList = closeList;
    }

    public void stopContainers(Set queueClosedSet){
        Collection<MessageListenerContainer> containers = this.getRegistry().getListenerContainers();
        SimpleMessageListenerContainer simpleContainer = null;
        for (MessageListenerContainer container : containers){
            simpleContainer = (SimpleMessageListenerContainer) container;
            String[] queueNames = simpleContainer.getQueueNames();
            for (String queueName : queueNames){
                if (queueClosedSet.contains(queueName)){
                    container.stop();
                }
            }
        }
    }
    public void startContainers(Set queueClosedSet){
        Collection<MessageListenerContainer> containers = this.getRegistry().getListenerContainers();
        SimpleMessageListenerContainer simpleContainer = null;
        for (MessageListenerContainer container : containers){
            simpleContainer = (SimpleMessageListenerContainer) container;
            String[] queueNames = simpleContainer.getQueueNames();
            for (String queueName : queueNames){
                if (!queueClosedSet.contains(queueName)){
                    container.start();
                }
            }
        }
    }

}
/**************************************************************************************************************************/
//@Component
//@ConfigurationProperties(prefix = "rabbitmq.consumer")
//public class RabbitmqConsumerSwitch implements InitializingBean,ApplicationContextAware {
//    private ApplicationContext context;
//    private RabbitListenerEndpointRegistry registry;
//    private String state;
//
//    public String getState() {
//        return state;
//    }
//
//    public void setState(String state) {
//        this.state = state;
//    }
//
//    public RabbitListenerEndpointRegistry getRegistry(){
//        return this.registry;
//    }
//    @EventListener
//    public void onAsyncConsumerStartedEvent(AsyncConsumerStartedEvent event){
//        if (state.equalsIgnoreCase("start")) return;
//        Collection<MessageListenerContainer> containers = this.getRegistry().getListenerContainers();
//        for (MessageListenerContainer container : containers){
//            container.stop();
//        }
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        if (state.equalsIgnoreCase("stop")) {
//            Collection<MessageListenerContainer> containers = this.getRegistry().getListenerContainers();
//            for (MessageListenerContainer container : containers){
//                container.stop();
//            }
//        }
//        if (state.equalsIgnoreCase("start")) {
//            Collection<MessageListenerContainer> containers = this.getRegistry().getListenerContainers();
//            for (MessageListenerContainer container : containers){
//                container.start();
//            }
//        }
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.context = applicationContext;
//        this.registry = this.context.getBean(RabbitListenerEndpointRegistry.class);
//    }
//}
