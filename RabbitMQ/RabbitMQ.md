# RabbitMQ实战 读书笔记

### 安装


rabbitmq启动方式有2种

1、以应用方式启动

rabbitmq-server -detached 后台启动

Rabbitmq-server 直接启动，如果你关闭窗口或者需要在改窗口使用其他命令时应用就会停止

关闭:rabbitmqctl stop

2、以服务方式启动（安装完之后在任务管理器中服务一栏能看到RabbtiMq）

rabbitmq-service install 安装服务

rabbitmq-service start 开始服务

Rabbitmq-service stop  停止服务

Rabbitmq-service enable 使服务有效

Rabbitmq-service disable 使服务无效

rabbitmq-service help 帮助

当rabbitmq-service install之后默认服务是enable的，如果这时设置服务为disable的话，rabbitmq-service start就会报错。

当rabbitmq-service start正常启动服务之后，使用disable是没有效果的

关闭:rabbitmqctl stop

3、Rabbitmq 管理插件启动，可视化界面

rabbitmq-plugins enable rabbitmq_management 启动

rabbitmq-plugins disable rabbitmq_management 关闭


4、Rabbitmq节点管理方式
Rabbitmqctl


### Rabbit基本概念
RabbitMQ底层为erlang语言，是AMQP（高级消息队列协议）的标准实现。
RabbitMQ消息路由必须有三部分: 交换器、队列、绑定
RabbitMQ使用流程: 
  1. 客户端连接到RabbitMQ服务器，打开一个channel(管道)。
  2. 客户端声明一个exchange(交换器)，并设置相关属性。
  3. 客户端声明一个queue(队列)，并设置相关属性。
  4. 客户端使用routing key(路由键)，在exchange和queue之间建立好绑定关系。
  5. 客户端投递消息到exchange
  6. exchange(交换器)接收到消息后，就根据消息的routing key(路由键)和已经设置的binding(绑定)，进行消息路由，将消息投递到一个或多个队列里。

> 管道的建立: 客户端与RabbitMQ服务器创建一条TCP连接。当TCP连接打开，客户端可以创建一个(channel)管道，channel是建立在TCP内的虚拟连接。AMQP命令通过channel发送。

##### Rabbit交换器类型
一共有四种类型: direct、fanout、topic、headers
- direct: 根据routing key(路由键)进行匹配，投递到对应的queue(队列)
- fanout: 当你发送一条消息到fanout交换器时，它会把消息投递给所有附加在此交换器上的队列。 这允许你对单条消息做不同方式的反应。
- topic: 使用'#'、'\*'、'.' 进行名字匹配，然后投递到对应的队列。 
        单个'.'把路由键分为了几部分，'\*'匹配特定位置的任意文本，'#'匹配所有规则

##### RabbitMQ虚拟主机
vhost: 每一个RabbitMQ服务器都能创建虚拟消息服务器，host本质上是一个mini版的RabbitMQ服务器，拥有自己的队列、交换器和绑定，并且拥有自己的权限机制。vhost之于Rabbit就像虚拟机之于物理服务器一样：它们通过在各个实例间提供逻辑上分离， 允许你为不同应用程序安全保密地运行数据。
> 它既能将同一Rabbit的众多客户区分开来，又可以避免队列和交换器的命名冲突。否则你可能不得不运行多个Rabbit, 并忍受随之而来头疼的管理问题。 相反，你可以只运行一个Rabbit, 然后按需启动或关闭vhost。

##### RabbitMQ持久化
每个队列和交换器的durable属性，决定了RabbitMQ是否需要在崩溃或者重启之后重新创建队列（或者交换器）。该属性默认情况为false, 将它设置为true这样你就不需要在服务器断电后重新创建队列和交换器了。
一条持久化消息必须被发布到持久化的交换器中并到达持久化的队列中才行。如果不是这样的话，则包含持久化消息的队列（或者交换器）会在Rabbit崩溃重启后不复存在，从而导致消息成为孤儿。因此，如果消息想要从RabbitMQ崩溃中恢复， 那么消息必须：
- 把它的投递模式选项设置为2(持久）
- 发送到持久化的交换器
- 到达持久化的队列
RabbitMQ确保持久性消息能从服务器重启中恢复的方式是，将它们写入磁盘上的一个持久化日志文件。当发布一条持久性消息到持久交换器上时，Rabbit会在消息提交到日志文件后才发送响应。
持久化带来的缺陷: 会极大地减少RabbitMQ服务器每秒可处理的消息总数。

##### 集群
RabbitMQ最优秀的功能之一就是其内建集群, 集群可以允许消费者和生产者在Rabbit节点崩溃的情况下继续运行，以及通过添加更多的节点来线性扩展消息通信吞吐量。

RabbitMQ 有三种模式：单机模式、普通集群模式、镜像集群模式。
  - 单机模式 一般为本地测试使用
  - 普通集群模式 高吞吐, 不高可用
  - 镜像集群模式 高可用

RabbitMQ 会记录以下四种类型的内部元数据：
  - 队列元数据: 队列名称和它们的属性（是否可持久化, 是否自动删除）
  - 交换器元数据: 交换器名称、 类型和属性（可持久化等）
  - 绑定元数据: 一张简单的表格展示了如何将消息路由到队列
  - vhost元数据: 为vhost 内的队列、 交换器和绑定提供命名空间和安全属性

**普通集群模式**
在单一节点内，RabbitMQ会将所有信息存储在内存中，同时将可持久化的队列和交换器（以及它们的绑定）存储到硬盘上。

但是如果在集群中创建队列的话。只有队列的所有者节点知道有关队列的所有信息, 所有其他非所有者节点只知道队列的元数据和指向该队列存在的那个节点的指针。

在集群中，可以选择配置部分节点为内存节点。 它使得像队列和交换器声明之类的操作更加快速.

**镜像集群模式**
在镜像集群模式下，创建的 queue，无论元数据还是 queue 里的消息都会存在于多个实例上，每个 RabbitMQ 节点都有这个 queue 的一个完整镜像，包含 queue 的全部数据的意思。然后每次写消息到 queue 的时候，都会自动把消息同步到多个实例的 queue 上。
















