# PCIP（Preferred Cloudflare IP）

首选 Cloudflare IP。

## 功能

- [x] 首选 Cloudflare IP。
- [x] 生成 Excel 结果.

## 使用

### 运行

JDK 17 环境开箱即用（Out of the box），代码很简单，没有过度封装。

运行后控制台会输出进度以及延迟最低的 5 个 IP。

> [!NOTE]
> 有可能会遇见程序挂起的现象，是因为默认开启了基于 CPU 核心数 * 200 的线程数，可通过更改线程数量解决.
> 首选 IP 的延迟默认为 200ms，会过滤延迟高于此数值的 IP。
> IP 段文件存放在 `src/main/resources/ips-v4`，来源 [IP 地址范围 | Cloudflare](https://www.cloudflare-cn.com/ips-v4)。
