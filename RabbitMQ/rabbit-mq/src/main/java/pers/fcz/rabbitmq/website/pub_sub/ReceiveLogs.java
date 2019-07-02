package pers.fcz.rabbitmq.website.pub_sub;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * 发布订阅模式 订阅者
 * @author Mr.F
 * @since 2019/7/2 15:48
 **/
public class ReceiveLogs {

    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 创建一个交换器并绑定类型
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        // 创建一个队列
        // 当没有向queueDeclare()提供参数时，会创建临时队列。是一个随机名称，非持久的，独占的自动删除队列
        String queueName = channel.queueDeclare().getQueue();
        // 队列绑定
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}
