package pers.fcz.rabbitmq.website.rpc;

import com.rabbitmq.client.*;

/**
 * RPC 模式 服务器
 * 1. 订阅rpc_queue队列, 获取消息 可以得到replyQueueName, correlationId
 * 2. 处理消息, 向replyQueueName发送消息 参数包含 correlationId
 *
 * @author Mr.F
 * @since 2019/7/29 09:48
 **/
public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    private static int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }


    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

            channel.queuePurge(RPC_QUEUE_NAME);
            // 每次只处理一个消息
            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");

            Object monitor = new Object(); // 锁
            // 回调队列 构造
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                /* correlationId
                   可以对每一个请求设置一个唯一的值
                   通过获取客户端的 correlationId , 初始化当前
                 */
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String response = "";

                try {
                    // 获取来的值
                    String message = new String(delivery.getBody(), "UTF-8");
                    int n = Integer.parseInt(message);
                    System.out.println(" [.] fib(" + message + ")");
                    // 计算后 放入返回的结果中
                    response += fib(n);
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e.toString());
                } finally {
                    // 发送请求 发送给ReplyTo(应答队列)
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    // ACK
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> { }));
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
