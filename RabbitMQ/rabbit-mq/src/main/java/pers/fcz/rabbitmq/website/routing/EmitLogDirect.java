package pers.fcz.rabbitmq.website.routing;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

/**
 * RabbitMQ官网 教程 Routing模式 (发送消息)
 * @author Mr.F
 * @since 2019/7/5 09:33
 **/
public class EmitLogDirect {

    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            while (true) {
                Scanner scanner = new Scanner(System.in);
                // 队列名称
                System.out.println("Please print bind: ");
                String severity = scanner.nextLine();;
                // 消息
                System.out.println("Please send message: ");
                String message = scanner.nextLine();

                channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + severity + "':'" + message + "'");
            }

        }
    }

}
