package pers.fcz.rabbitmq.web.start;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

/**
 * @author Mr.F
 * @since 2019/6/21 10:34
 **/
public class Recv {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        // 创建 Connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 查询状态
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 接收数据
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    }

}
