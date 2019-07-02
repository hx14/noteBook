package pers.fcz.rabbitmq.website.work_queues;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

/**
 * @author Mr.F
 * @since 2019/7/2 14:44
 **/
public class Send {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) {
        // 创建一个 Connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             //  create channel
             Channel channel = connection.createChannel()) {
            // 第二个字段 为是否进行持久化 (无法修改已经建立的队列)
            // 第三个字段 是否为当前连接的专用队列，在连接断开后，会自动删除该队列 ( 为True表示只能有一个生产者 消费者)
            // 第四个字段 当没有任何消费者使用时，自动删除该队列。
            // 第五个字段 消息配置
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            while (true) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Please send message: ");
                String message = scanner.nextLine();
                if (message.equals("exit")) {
                    break;
                }
                // 第三个字段设置为 MessageProperties.PERSISTENT_TEXT_PLAIN 可以进行持久化传输Message
                channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());

                System.out.println("Sent '" + message + "' success");
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
