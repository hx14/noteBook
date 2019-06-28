package pers.fcz.rabbitmq.web.start;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

/**
 * @author Mr.F
 * @since 2019/6/21 10:30
 **/
public class Send {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) {
        // 创建一个 Connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //  create channel
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";
            for (int i = 0; i < 100; i++) {
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            }
            System.out.println(" [x] Sent '" + message + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
