# PCIP（Preferred Cloudflare IP）

首选 Cloudflare IP。

## 功能

- [x] 首选 Cloudflare IP。
- [x] 首选 Cloudflare WARP IP。
- [x] 生成 Excel 结果.

## 使用

> [!NOTE]
> master 分支首选 Cloudflare IP，warp 分支首选 Cloudflare WARP IP。

JDK 21 环境开箱即用（Out of the box），代码很简单，没有过度封装。

```shell
# 首选 Cloudflare IP
java -jar pcip-1.1.0.jar
# 首选 Cloudflare WARP IP。
java -jar pcip-1.1.0-warp.jar
```

### 构建

```shell
mvn clean package -DskipTests
```

### 运行

```shell
# 首选 Cloudflare IP
java -jar target/pcip-1.1.0.jar
# 首选 Cloudflare WARP IP。
java -jar target/pcip-1.1.0-warp.jar
```

运行后控制台会输出进度以及延迟最低的 5 个 IP。

> [!NOTE]
> Cloudflare IP 结果集中的延迟不是 Ping ICMP 协议的结果，是 Socket Port 7 Echo 服务的结果。Cloudflare WARP IP 结果集在 root 权限下是 Ping ICMP 协议的结果。

> [!NOTE]
> 首选 IP 的延迟默认为 200ms，首选 WARP IP 的延迟默认为 5000ms，会尽量过滤延迟高于此数值的 IP。

> [!NOTE]
> IP 段来源 [IP 地址范围 | Cloudflare](https://www.cloudflare-cn.com/ips-v4)。

> [!NOTE]
> WARP IP 段来源 [WARP with firewall · Cloudflare Zero Trust docs](https://developers.cloudflare.com/cloudflare-one/connections/connect-devices/warp/deployment/firewall/)。
