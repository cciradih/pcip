# PCIP（Preferred Cloudflare IP）

首选 Cloudflare IP。

## 功能

- [x] 首选 Cloudflare IP。
- [x] 生成 Excel 结果.

## 使用

JDK 21 环境开箱即用（Out of the box），代码很简单，没有过度封装。

```shell
java -jar pcip-1.1.0.jar 
```

### 构建

```shell
mvn clean package -DskipTests
```

### 运行

```shell
java -jar target/pcip-1.1.0.jar 
```

运行后控制台会输出进度以及延迟最低的 5 个 IP。

> [!NOTE]
> 结果集中的延迟不是 Ping ICMP 协议的结果，是 Socket Port 7 Echo 服务的结果。

> [!NOTE]
> 首选 IP 的延迟默认为 200ms，会尽量过滤延迟高于此数值的 IP。

> [!NOTE]
> IP 段来源 [IP 地址范围 | Cloudflare](https://www.cloudflare-cn.com/ips-v4)。
