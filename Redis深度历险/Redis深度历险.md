# Redis深度历险 读书笔记

### Redis基础数据结构
String(字符串)、 list(列表)、 hash(字典)、set(集合)、zset(有序集合
##### String
Redis的字符串是动态字符串，可以进行修改，内部结构实现类似于ArrayList，采用预分配冗余控件的方式减少内存的频繁分配。**字符串的最大长度为512MB** 
- 添加一个字符串 set name value
- 获取一个字符串 get name
- 判断是否存在 exists name
- 删除 del name
- 添加多个字符串 mset name1 value1 name2 value2
- 获取多个字符串 mget name1 name2
- 设置过期时间(秒) expire name 5
- 自增 incr name 10 自增存在范围, 范围在signed long的最大值最小值之间，超出会报错。
> 字符串由多个字节组成，每个字节由8个bit组成。

##### List
Redis的list类似于LinkedList，由链表组成。插入和删除很快，时间复杂度为O(1)。定位较慢，时间复杂度为O(n)。
Redis通常用来做异步队列使用。
- 先进先出(队列)
  - push操作 rpush listname value1 value2 value3
  - pop操作 lpop listname
- 先进后出(栈)
  - push操作 rpush listname value1 value2 value3
  - pop操作 rpop listname
- 通过下标获取 lindex listname 1
- 获取范围内元素 lrange listname 0 1
- 截取范围内元素 ltrim listname 0 1

##### Hash
Redis的Hash类似于HashMap，是一个无序字典，内部存储键值对。
- 添加 hset hash key value
- 添加多个 hmset hash key1 value1 key2 value2
- 获取单个 hget hash key
- 获取全部 hgetall hash
- 长度 hlen hash

##### Set
Redis的set类似于Java的HashSet，具有去重功能。
- 添加 sadd set value1
- 获取一个 spop set
- 获取全部 smembers set
- 判断是否存在 sismember set value1
- 获取长度 scard set

##### zset
Redis的zset类似于SortedSet和HashSet的结合体，一方面是一个set，另外给每一个value赋予一个score，代表排序权重。
- 添加 zadd zset 9.0 value1
- 按照score列出 zrange zset 0 -1
- 倒序列出 zrevrange zset 0 -1 
- 获取value的score zscore zset value
- 排名 zrange zset value1
- 根据分值区间遍历 zrangebyscore zset 0 9.0
- 删除 zrem zset value1 
> zset内部排序通过“跳跃列表”实现。

list,set,hash,zset 四种数据结构是容器型数据结构。
- 1. create if not exists: 如果不存在就会新建一个进行操作。
- 2. drop if no elements: 容器中没有元素就会删除。

> 过期时间：Redis所有的数据结构都可以设置过期时间，时间到了会自动删除。过期以对象为单位，会删除整个数据结构，而不是删除其中的元素。如果一个字符串已经设置了过期时间，再调用set，过期时间会消失。

### Redis应用

##### 分布式锁
分布式应用在进行逻辑处理时，存在竞争，会出现并发问题。需要使用分布式锁来限制程序。
使用setnx(set if not exists)指令，只允许被一个客户端使用，使用后再调用del指令移除。当程序运行中出现异常或导致del指令不会执行，陷入死锁。因此可以加入一个过期时间，使用expire指令。
如果服务器在setnx和expire中间崩溃，expire不会执行，也会造成死锁。因此需要其他方法保证其原子性。
> 在Redis 2.8版本加入了set指令，使setnx和expire指令可以一起执行。set lock:key true ex 5 nx

超时问题: 如果某一段程序运行过慢，在expire期间并没有执行完，第二个线程就会提前获取锁。因此，Redis尽量不要用于较长时间的任务。

可重入性：如果一个锁支持同一个线程多次加锁，那么这个锁就是可重入的。Redis分布式锁如果想支持可重入，需要对客户端的set方法进行包装，使用线程安全类Threadlocal变量存储当前持有锁的计数。

##### 延时队列
Redis可用list实现一个异步消息队列，使用rpush和lpush入队，lpop和rpop出队。但是没有ack确认，不能保证消息的可靠性。
如果队列空了，pop操作会陷入死循环，会造成提高客户端CPU消耗。可以使用blpop或brpop指令，字符b代表的是blocking，也就是***阻塞读***。阻塞读在队列没有数据的时候立即进入休眠状态，数据到来继续操作。
> 当线程一直阻塞，Redis连接会变成闲置连接。闲置过久会与服务器断开连接，这个时候blpop或brpop会抛出异常。编写代码需要捕获异常进行处理。
延时队列的实现: 可以通过zset实现，消息为value，到期处理时间为score，然后使用多个线程轮询zset获取到期任务进行处理。多个线程保障可用性，也会带来并发问题。

##### 位图
位图不是特殊的数据结构，内容就是普通的字符串，也就是byte数组。可以使用普通的get\set方法获取和设置。也可以使用getbit\setbit将byte数组看成“位数组”处理。
- Redis的位数组是自动填充的，如果某个偏移量超出了现有的内容范围，将会用0填充。
- 设置字符串时只需要设置值为的位。
- 可以进行按单个位或者整体set或get。
- 可以通过bitfield操作多个位。并且可以混合执行多个set/get/incrby指令。
- Redis提供了位图统计指令bitcount，用于统计指定位置范围1的个数。

###### HyperLogLog
HyperLogLog是Redis的高级数据结构,用来解决统计问题(不精确的去重计数)，需要Redis版本>=2.8.9。例如，网站的浏览量、商品的销量。
HyperLogLog提供两个指令pfadd(增加计数)、pfcount(获取计数)、pfmerge(合并计数)。
HyperLogLog在计数比较少时，采用稀疏矩阵存储，空间占用很少。当占用空间超过阈值，就会转变成稠密矩阵，会占用12kb空间。

##### 布隆过滤器
布隆过滤器用来解决去重问题(有一定的误判概率)，需要Redis版本>=4.0。适用于大量用户的门户网站，可以判断出用户已经看过的内容。
布隆过滤器有两个基本指令，bf.add(添加单个元素) bf.madd(添加多个元素) bf.exists(查询单个元素是否存在) bf.mexists(查询多个元素是否存在)
布隆过滤器的误判率大概为1%，可以使用bf.reserver指令进行配置，降低误判率。
布隆过滤器原理：数据结构为一个大型的位数组和几个不一样的无偏hash函数(能够把元素的hash值计算的比较均匀，位置更加随机)。
添加值时，会用多个hash函数对key进行hash，然后对数组长度取模得到位置，每个hash函数会算的一个不同的位置。然后把位数组的这几个位置都置1，就完成了添加。
查询是否存在时，也是这么计算位置，看看位数组中这几个位置是否都为1，如果有一个为0，就不存在。
> 存在hash冲突问题，当查询是否存在的位置时，可能是别的key存在导致的。  这也是存在误判的原因。

##### Redis-Cell
Redis4.0提供的一个限流模块，使用了漏斗算法，提供了原子的限流指令。

##### scan指令
2.8版本加入，通过游标分步进行，不会阻塞线程。提供limit参数，不会一次返回过多的数据。返回的结果可能会有重复，需要进行去重。

### Redis原理篇
Redis是一个高并发的***单线程***中间件。通过非阻塞IO，多路复用处理多客户端的连接。它所有的**数据都在内存中所有操作都是内存级别**

非阻塞IO: 意味线程在读写时可以不必阻塞，读写可以瞬间完成。

多路复用: 最简单的API是select函数，输入读写描述符列表，输出与之对应的可读可写的事件。同时提供了一个timeout参数，如果没有任何事件到来，最多等待timeout的时间。一旦期间有任何时间到来，就可以立即返回。时间过去之后，也会立即返回。 

> 现代操作系统的多路复用API已经不使用select调用，改用epoll(linux)和kqueue

##### 持久化
Redis的持久化有两种, RDB(快照)和AOF日志，两者各有优缺点。

  - RDB: 在指定的时间间隔能对数据进行全量备份。
    优点:
      1. 适用于数据集的备份。假如每天备份前一个月的数据, 出现问题后可回退到不同时间线的版本
      2. RDB是一个紧凑的单一文件, 可以直接进行文件传输，解决问题
      3. 与AOF相比,在恢复大的数据集的时候, RDB 方式会更快一些
    缺点:
      1. 会隔指定的时间才会进行备份, 因此在时间间隔内会丢失部分数据
      2. RDB需要使用**fork**子进程进行快照。当数据集比较大时, fork非常耗时的,可能会导致Redis在一些毫秒级内不能响应客户端的请求

  fork(多进程): Redis在持久化时会调用glibc的函数fork产生一个子进程,快照持久化给予子进程处理，父进程处理客户端请求。父进程中对数据的修改, 不会影响到子进程持久化,子进程的数据在产生进程的一瞬间就凝固了,不会改变。父进程修改时会将数据复制分离出来。

  - AOF: 是连续的增量备份, 存储的是Redis服务器的顺序指令序列, 只记录对内存修改的操作。当服务器重启的时候会重新执行这些命令来恢复原始的数据。
  	优点:
      1. AOF是一个只进行追加的文件, 更容易维护
      2. AOF文件有序地保存了对数据库执行的所有写入操作，这些写入操作以Redis协议的格式保存，更易读。
    缺点:
      1. 与RDB相比，AOF生成的文件更大。文件较大时, 可以人工维护。提供了bgrewriteaof用于对AOF日志的瘦身.
      2. 因为**fsync**的原因, 速度相比RDB也会较慢

  fsync: 当程序对AOF进行写操作时,如果机器突然故障, 那么AOF日志可能没完全刷到磁盘中, 会出现日志丢失。因此, Liunx的glibc提供了fsync函数, 可以指定文件内容强制刷到磁盘。但是fsync是一个IO操作,速度很慢。通常Redis每个1s左右执行一次fsync操作，1s的周期可以进行配置, 也可以用不调用fsync.

RDB 保存结果是单一紧凑文件，可以将文件备份，并且在恢复大量数据的时候，RDB方式的速度会比 AOF 方式的回复速度要快。但是由于备份频率不高，所以在回复数据的时候有可能丢失一段时间的数据，而且在数据集比较大的时候有可能对毫秒级的请求产生影响。

AOF 以顺序追加的方式记录操作日志，文件内容易读。fsync保证了AOF的可靠性, 默认每秒钟备份1次。当日志文件较大时, 速度会稍慢。

> 当然 如果你的数据只是在服务器运行的时候存在,你也可以不使用任何持久化方式。

Redis4.0 混合持久化: 将RDB和AOF日志村在一起。AOF不再是全量的日志, 而是自持久化开始到持久化结束这段时间发生的增量AOF日志。这样在Redis重启时,先加载RDB再加载AOF,效率大幅度的提高。








































