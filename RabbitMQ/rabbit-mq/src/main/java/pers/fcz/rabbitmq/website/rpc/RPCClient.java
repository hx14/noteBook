package pers.fcz.rabbitmq.website.rpc;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * RCP 模式 客户端
 * 1. 向rpc_queue队列发送消息, 参数包含replyQueueName, correlationId
 * 2. 订阅 replyQueueName 队列，等待消息. 通过判断correlationId是否为同一次的响应
 *
 * @author Mr.F
 * @since 2019/7/29 09:49
 **/
public class RPCClient implements AutoCloseable  {


    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public static void main(String[] argv) {
        // 初始化
        try (RPCClient fibonacciRpc = new RPCClient()) {
            for (int i = 0; i < 32; i++) {
                String i_str = Integer.toString(i);
                System.out.println(" [x] Requesting fib(" + i_str + ")");
                String response = fibonacciRpc.call(i_str);
                System.out.println(" [.] Got '" + response + "'");
            }
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String call(String message) throws IOException, InterruptedException {
        // 获取一个唯一的correlationId
        final String corrId = UUID.randomUUID().toString();
        // 应答队列声明
        String replyQueueName = channel.queueDeclare().getQueue();
        // 构造参数
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes(StandardCharsets.UTF_8));
        // 应答队列构造
        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 通过correlationId 判断是否为同一次
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        };
        String ctag = channel.basicConsume(replyQueueName, true, deliverCallback, consumerTag -> { });

        String result = response.take();
        // 关闭
        channel.basicCancel(ctag);
        return result;
    }

    public void close() throws IOException {
        connection.close();
    }

}
