package pers.fcz.rabbitmq.website.topic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

/**
 * RabbitMQ官网教程 Topic 模式 (发送消息)
 * @author Mr.F
 * @since 2019/7/26 10:55
 **/
public class EmitLogTopic {

    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, "topic");

            while (true) {
                Scanner scanner = new Scanner(System.in);
                // 队列名称
                System.out.println("Please print routingKey: ");
                String routingKey = scanner.nextLine();;
                // 消息
                System.out.println("Please send message: ");
                String message = scanner.nextLine();

                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
            }
        }
    }

}
