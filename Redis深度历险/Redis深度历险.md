# Redis深度历险 读书笔记

### 五种基础数据结构
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



















