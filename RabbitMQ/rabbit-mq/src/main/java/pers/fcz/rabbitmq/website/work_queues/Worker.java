package pers.fcz.rabbitmq.website.work_queues;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

/**
 * 使用多个Worker高效的执行任务
 * 1. 消息确认
 *    如果某一个Worker在执行任务中途失败，将会把任务交给另一个Worker
 *    Worker完成任务后将会发回ack
 *
 * @author Mr.F
 * @since 2019/6/21 10:34
 **/
public class Worker {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        // 创建 Connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 在处理并确认前一个消息之前，不要向工作人员发送新消息
        channel.basicQos(1);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" Received '" + message + "' success");
            try {
                doWork(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Queue None Message");
            }
        };
        boolean autoAck = true; // acknowledgment is covered below
        channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });

        System.out.println(" Waiting for messages. To exit press CTRL+C");
    }

    private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()) {
            if (ch == '.')
                Thread.sleep(1000);
        }
    }

}
