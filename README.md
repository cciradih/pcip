# PCIP（Preferred Cloudflare IP）

首选 Cloudflare IP。

## 功能

- [x] 首选 Cloudflare IP。
- [x] 生成 Excel 结果.

## 使用

JDK 17 环境开箱即用（Out of the box），代码很简单，没有过度封装。

```shell
java -jar pcip-1.0.1.jar 
```

### 构建

```shell
mvn clean package -DskipTests
```

### 运行

```shell
java -jar target/pcip-1.0.1.jar 
```

运行后控制台会输出进度以及延迟最低的 5 个 IP。

> [!NOTE]
> 参考 [InetAddress.isReachable源码学习](https://aiziyuer.github.io/2016/08/14/openjdk-study-java-api-isreachable.html)
> 建议在 root 环境下运行。

> [!NOTE]
> 有可能会遇见程序挂起的现象，是因为默认开启了基于 CPU 核心数 * 200 的线程数，可通过更改线程数量解决.

> [!NOTE]
> 首选 IP 的延迟默认为 200ms，会过滤延迟高于此数值的 IP。

> [!NOTE]
> IP 段文件存放在 `src/main/resources/ips-v4`，来源 [IP 地址范围 | Cloudflare](https://www.cloudflare-cn.com/ips-v4)。
