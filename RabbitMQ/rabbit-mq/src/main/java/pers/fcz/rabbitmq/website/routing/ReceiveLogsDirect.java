package pers.fcz.rabbitmq.website.routing;
import com.rabbitmq.client.*;

import java.util.Scanner;

/**
 * RabbitMQ官网 教程 Routing模式 (接受消息)
 * @author Mr.F
 * @since 2019/7/5 09:34
 **/
public class ReceiveLogsDirect {


    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 声明交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        // 获取队列名称
        String queueName = channel.queueDeclare().getQueue();

        // 绑定 队列 到 交换器
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String severity = scanner.nextLine();
            if (severity.equals("exit")) {
                break;
            }
            channel.queueBind(queueName, EXCHANGE_NAME, severity);
        }
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

}
