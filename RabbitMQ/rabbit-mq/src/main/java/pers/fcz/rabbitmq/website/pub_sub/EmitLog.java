package pers.fcz.rabbitmq.website.pub_sub;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

/**
 * 发布订阅模式 发布者
 * @author Mr.F
 * @since 2019/7/2 15:48
 **/
public class EmitLog {

    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        // 1.创建连接 2.创建管道 3. 创建一个交换器并绑定类型
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            while (true) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Please send message: ");
                String message = scanner.nextLine();
                if (message.equals("exit")) {
                    break;
                }
                // 发布消息 指定交换器
                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
                System.out.println("Sent '" + message + "' success");
                System.out.println();
            }


        }
    }

}
