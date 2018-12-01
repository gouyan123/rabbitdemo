package com.example.rabbitdemo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import lombok.Data;

/**
 * MQ客户端监控器
 */
@Component
public class MQClientMonitor {

    private static final String CONTAINER_NOT_EXISTS = "消息队列%s对应的监听容器不存在！";

    private RabbitListenerEndpointRegistry registry;
    @Autowired
    public void registry(RabbitListenerEndpointRegistry registry){
        this.registry = registry;
    }

    /**
     * queue2ContainerAllMap初始化标识
     */
    private volatile boolean hasInit = false;

    /**
     * 所有的队列监听容器MAP
     */
    private final Map<String, SimpleMessageListenerContainer> allQueueContainerMap = new ConcurrentHashMap<>();

    /**
     * 重置消息队列并发消费者数量
     * @param queueName
     * @param concurrentConsumers must greater than zero
     * @return
     */
    public boolean resetQueueConcurrentConsumers(String queueName, int concurrentConsumers) {
        Assert.state(concurrentConsumers > 0, "参数 'concurrentConsumers' 必须大于0.");
        SimpleMessageListenerContainer container = findContainerByQueueName(queueName);
        if (container.isActive() && container.isRunning()) {
            container.setConcurrentConsumers(concurrentConsumers);
            return true;
        }
        return false;
    }


    /**
     * 重启对消息队列的监听
     */
    public boolean restartMessageListener(String queueName) {
        SimpleMessageListenerContainer container = findContainerByQueueName(queueName);
        Assert.state(!container.isRunning(), String.format("消息队列%s对应的监听容器正在运行！", queueName));
        container.start();
        return true;
    }

    /**
     * 停止对消息队列的监听
     */
    public boolean stopMessageListener(String queueName) {
        //findContainerByQueueName(queueName) 找到监听 该队列的 消费者集合
        SimpleMessageListenerContainer container = findContainerByQueueName(queueName);
        Assert.state(container.isRunning(), String.format("消息队列%s对应的监听容器未运行！", queueName));
        //关闭 消费者监听器 集合
        container.stop();
        return true;
    }

    /**
     * 统计所有消息队列详情
     * @return
     */
    public List<MessageQueueDatail> statAllMessageQueueDetail() {
        List<MessageQueueDatail> queueDetailList = new ArrayList<>();
//        getQueueContainerAllMap().entrySet().forEach(entry -> {
//            String queueName = entry.getKey();
//            SimpleMessageListenerContainer container = entry.getValue();
//            MessageQueueDatail deatil = new MessageQueueDatail(queueName, container);
//            queueDetailList.add(deatil);
//        });
        for (Map.Entry<String, SimpleMessageListenerContainer> entry : getQueueContainerAllMap().entrySet()){
            String queueName = entry.getKey();
            SimpleMessageListenerContainer container = entry.getValue();
            MessageQueueDatail deatil = new MessageQueueDatail(queueName, container);
            queueDetailList.add(deatil);
        }
        return queueDetailList;
    }

    /**
     * 根据队列名查找消息监听器的集合
     */
    private SimpleMessageListenerContainer findContainerByQueueName(String queueName) {
        String key = StringUtils.trim(queueName);
        //
        SimpleMessageListenerContainer container = getQueueContainerAllMap().get(key);
        Assert.notNull(container, String.format(CONTAINER_NOT_EXISTS, key));
        return container;
    }
    /**
     * getQueueContainerAllMap() 将队列名称作为key，监听队列的消费端监听器集合作为value存入allQueueContainerMap
     * */
    private Map<String, SimpleMessageListenerContainer> getQueueContainerAllMap() {
        //hasInit表示 allQueueContainerMap集合是否初始化
        if (!hasInit) {
            //allQueueContainerMap 所有的队列的监听容器的集合
            synchronized (allQueueContainerMap) {
                if (!hasInit) {
                    //registry.getListenerContainers() 获取 队列监听者集合；
                    for (MessageListenerContainer container : registry.getListenerContainers()){
                        SimpleMessageListenerContainer simpleContainer = (SimpleMessageListenerContainer) container;
                        for (String queueName: simpleContainer.getQueueNames()){
                            allQueueContainerMap.putIfAbsent(StringUtils.trim(queueName), simpleContainer);
                        }
                    }
                    hasInit = true;
                }
            }
        }
        return allQueueContainerMap;
    }


    /**
     * 消息队列详情
     * @author liuzhe
     * @date 2018/04/04
     */
    @Data
    public static final class MessageQueueDatail {
        /**
         * 队列名称
         */
        private String queueName;

        /**
         * 监听容器标识
         */
        private String containerIdentity;

        /**
         * 监听是否有效
         */
        private boolean activeContainer;

        /**
         * 是否正在监听
         */
        private boolean running;

        /**
         * 活动消费者数量
         */
        private int activeConsumerCount;

        public MessageQueueDatail(String queueName, SimpleMessageListenerContainer container) {
            this.queueName = queueName;
            this.running = container.isRunning();
            this.activeContainer = container.isActive();
            this.activeConsumerCount = container.getActiveConsumerCount();
            this.containerIdentity = "Container@" + ObjectUtils.getIdentityHexString(container);
        }

    }

}