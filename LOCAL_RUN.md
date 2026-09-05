# 电商订单交易系统本地启动

本项目基于 `macrozheng/mall` 二次开发，当前本地化目标是先跑通订单交易系统的最小后端闭环：

- `mall-admin`: 后台管理接口，端口 `8081`
- `mall-portal`: 前台商城接口，端口 `8085`
- MySQL: 业务数据库，宿主机端口 `3307`
- Redis: 缓存，宿主机端口 `6381`
- RabbitMQ: 订单超时取消消息，宿主机端口 `5673`，管理台 `15673`
- MongoDB: 浏览记录等扩展数据，宿主机端口 `27018`
- MinIO: 本地对象存储，API `9091`，控制台 `9002`

## 1. 启动基础设施

WSL / Linux:

```bash
cd /mnt/c/Users/86131/Documents/Codex/2026-07-21/new-chat/ecommerce-order-system
cp .env.local.example .env.local
docker compose --env-file .env.local -f docker-compose.local.yml up -d
docker compose --env-file .env.local -f docker-compose.local.yml ps
```

PowerShell:

```powershell
cd C:\Users\86131\Documents\Codex\2026-07-21\new-chat\ecommerce-order-system
copy .env.local.example .env.local
docker compose --env-file .env.local -f docker-compose.local.yml up -d
docker compose --env-file .env.local -f docker-compose.local.yml ps
```

MySQL 容器首次启动时会自动执行 `document/sql/mall.sql`。如果 `order_mall_mysql_data` 卷已经存在，SQL 不会重复导入。

## 2. 启动后端服务

先编译并打包后台管理接口：

```powershell
mvn -pl mall-admin -am -DskipTests package
```

项目已通过 `.mvn/maven.config` 将 Maven 本地仓库固定到 `.mvn/repository`，避免使用全局 `D:\develop\repository` 时因为目录不存在导致依赖下载失败。
同时默认设置 `docker.skip=true`，避免 Maven 打包阶段触发原项目的 Docker 镜像构建并连接 `192.168.3.101:2375`。

启动后台管理接口：

```powershell
java -jar mall-admin\target\mall-admin-1.0-SNAPSHOT.jar --spring.profiles.active=local
```

打包前台商城接口：

```powershell
mvn -pl mall-portal -am -DskipTests package
```

启动前台商城接口：

```powershell
java -jar mall-portal\target\mall-portal-1.0-SNAPSHOT.jar --spring.profiles.active=local
```

如果一定要使用 `spring-boot:run`，需要先安装依赖模块，再去掉 `-am`：

```powershell
mvn -pl mall-admin -am -DskipTests install
mvn -pl mall-admin spring-boot:run "-Dspring-boot.run.profiles=local"
```

## 3. 常用地址

- mall-admin API: `http://127.0.0.1:8081`
- mall-admin Swagger: `http://127.0.0.1:8081/swagger-ui/`
- mall-portal API: `http://127.0.0.1:8085`
- mall-portal Swagger: `http://127.0.0.1:8085/swagger-ui/`
- RabbitMQ 管理台: `http://127.0.0.1:15673`
- MinIO 控制台: `http://127.0.0.1:9002`

## 4. 默认账号

后台管理用户：

```text
admin / macro123
```

前台会员用户可使用 SQL 中的测试用户，后续会补充登录和订单接口验证脚本。

## 5. 本地端口规划

为了避免和 RAG 客服项目冲突，本项目基础设施端口使用：

```text
MySQL: 3307
Redis: 6381
RabbitMQ: 5673 / 15673
MongoDB: 27018
MinIO: 9091 / 9002
```

下一阶段二次开发重点：

- 精简并强化订单交易链路
- 补订单状态机说明
- 补支付回调幂等和订单超时关闭验证
- 暴露客服 Agent 可调用的订单、物流、退款接口
