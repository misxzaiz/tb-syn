# 数据同步框架使用指南

## 概述

本框架提供了一个可复用的数据同步引擎，支持灵活的存储后端和业务逻辑扩展。

## 核心架构

```
core/
├── api/                    # 框架核心接口
│   ├── SynConfigService    # 配置存储接口
│   ├── SynQueueService     # 队列服务接口
│   └── DataProcessor       # 数据处理接口
├── SyncEngine              # 同步引擎接口
├── DefaultSyncEngine       # 默认引擎实现
└── SyncEngineFactory       # 引擎工厂
```

## 快速开始

### 1. 实现核心接口

```java
// 配置存储实现
@Service
public class MySQLConfigService implements SynConfigService {
    @Autowired
    private ConfigRepository configRepository;

    @Override
    public void saveSynConfigDTO(SynConfigDTO config) {
        // 保存到数据库
    }

    @Override
    public Optional<SynConfigDTO> getSynConfigDTO(String cid) {
        // 从数据库读取
    }
}

// 队列服务实现
@Service
public class RabbitMQQueueService implements SynQueueService<YourDataType> {
    @Override
    public void leftPush(String prefix, String cid, List<YourDataType> dataList) {
        // 推送到RabbitMQ
    }

    @Override
    public YourDataType popAndBak(String prefix, String cid) {
        // 从RabbitMQ弹出并备份
    }

    // 实现其他方法...
}

// 数据处理实现
@Service
public class YourDataProcessor implements DataProcessor<YourDataType> {
    @Override
    public TotalPageDTO<YourDataType> process(PageReqDTO req) {
        // 实现具体的业务查询逻辑
    }
}
```

### 2. 使用同步引擎

```java
@Service
public class YourBusinessService {

    @Autowired
    private SyncEngineFactory engineFactory;

    @Autowired
    private YourDataProcessor dataProcessor;

    public void syncData(String cid) {
        // 获取同步引擎
        SyncEngine<YourDataType> engine = engineFactory.getEngine(
            YourDataType.class,
            dataProcessor
        );

        // 方式1：同步并消费
        engine.syncAndConsume(cid, data -> {
            // 处理每条数据
            System.out.println("处理数据: " + data);
        });

        // 方式2：仅同步到队列
        engine.syncOnly(cid);

        // 获取同步状态
        String status = engine.getSyncStatus(cid);
        System.out.println("同步状态: " + status);
    }
}
```

## 支持的存储后端

### 配置存储（SynConfigService）
- Redis（默认实现）
- MySQL/PostgreSQL
- 文件存储
- Zookeeper/etcd

### 队列存储（SynQueueService）
- Redis（默认实现）
- RabbitMQ
- Kafka
- RocketMQ
- 数据库表

## 扩展点

1. **自定义同步流程**
   - 继承DefaultSyncEngine
   - 实现SyncEngine接口

2. **自定义分页策略**
   - 扩展PageReqDTO
   - 实现特殊的分页逻辑

3. **自定义时间窗口**
   - 扩展QueryRequestBuilder
   - 支持复杂的同步策略

## 注意事项

1. 接口实现必须保证线程安全
2. 队列服务的popAndBak方法需要保证原子性
3. 配置存储需要支持高并发读写
4. 数据处理器的process方法应该支持分页查询