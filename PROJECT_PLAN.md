# 电商订单交易系统二次开发计划

## 项目定位

项目名称：电商订单交易系统

基础项目：`macrozheng/mall` 的 Spring Boot 单体版。

目标不是简单运行原项目，而是围绕订单交易主链路做二次开发和工程化整理，最终形成一个能和 RAG 智能客服项目联动的传统 Java 后端项目。

## 第一阶段：本地可运行闭环

状态：进行中。

已完成：

- 新增本地 `docker-compose.local.yml`，使用独立端口避免和 RAG 客服项目冲突。
- 新增 `application-local.yml`，让 `mall-admin` 和 `mall-portal` 连接本地 compose 基础设施。
- 新增 `.mvn/maven.config`，使用项目内 Maven 仓库，绕开全局 `D:\develop\repository` 路径问题。
- `mall-admin` 依赖链编译通过。
- `mall-portal` 依赖链编译通过。

待完成：

- 在 WSL 中启动本地 compose 基础设施。
- 验证 MySQL 自动导入 `document/sql/mall.sql`。
- 启动 `mall-admin` 和 `mall-portal`。
- 跑通登录、商品查询、购物车、订单查询等最小接口。

## 第二阶段：订单交易主链路强化

重点模块：

- 商品 SKU 查询
- 购物车
- 订单确认
- 订单创建
- 库存扣减
- 优惠券核销
- 支付回调模拟
- 订单超时取消
- 订单状态机

已完成：

- 新增 `OrderStatus` 订单状态枚举，集中维护 `待付款 -> 待发货 -> 已发货 -> 已完成/已关闭` 的状态含义和流转约束。
- 改造支付成功回调为幂等处理：只有待付款订单可以从 `0` 更新到 `1`，重复回调不会重复扣减库存。
- 改造取消订单、超时取消、确认收货、删除订单逻辑，统一使用状态机判断，避免越权状态流转。
- 改造后台发货逻辑，只允许待发货订单发货，并且只为实际发货成功的订单写入操作历史。
- 改造后台关闭订单逻辑，只允许待付款订单被运营关闭，避免已支付、已发货、已完成订单被误关闭。
- 新增 Redis 原子 `setIfAbsent` 能力，订单创建入口基于用户 ID 和提交参数生成业务指纹，使用 `SETNX + TTL` 防止重复提交。
- 新增 `/order/stockLockStatus/{orderId}` 验证接口，返回订单商品对应 SKU 的总库存、锁定库存和可售库存。
- 新增 `/order/timeoutCancel/send` 验证接口，支持手动发送指定毫秒数的 RabbitMQ 延迟取消消息。
- 新增 `/order/timeoutCancel/trace/{orderId}` 追踪接口，聚合订单状态、MQ 队列信息、库存锁定状态和订单操作日志。
- 在订单创建、发送延迟消息、支付成功、用户取消、MQ 超时关闭、确认收货等关键节点写入 `oms_order_operate_history`，形成交易状态变更审计链路。
- 新增 `ReturnApplyStatus` 售后退货申请状态机，约束 `待处理 -> 退货中/已拒绝 -> 已完成` 的审核流转。
- 增强后台售后审核接口：`/returnApply/audit/approve/{id}`、`/returnApply/audit/reject/{id}`、`/returnApply/audit/complete/{id}`。
- 售后审核通过、拒绝、退货完成会写入订单操作日志；退货完成时同步关闭订单。

状态流转：

```text
待付款(0)
  -> 支付成功 -> 待发货(1)
  -> 用户取消/超时取消/后台关闭 -> 已关闭(4)

待发货(1)
  -> 后台发货 -> 已发货(2)

已发货(2)
  -> 用户确认收货 -> 已完成(3)

已完成(3) / 已关闭(4)
  -> 用户删除订单 -> delete_status=1
```

支付幂等策略：

- 使用 MyBatis 条件更新：`id = ? AND delete_status = 0 AND status = 0`。
- 第一次支付成功时，订单状态改为待发货，并扣减真实库存、释放锁定库存。
- 重复支付回调查询到订单已是待发货时直接返回，不再扣减库存。
- 已关闭、已发货、已完成订单再次收到支付回调时拒绝处理，避免脏状态。

订单创建防重复提交策略：

- 防重维度：会员 ID、收货地址、优惠券、积分、支付方式、购物车 ID 集合。
- 锁实现：Redis `SETNX`，Key 形如 `mall:oms:orderSubmitLock:{memberId}:{sha256}`。
- 锁过期时间：默认 10 秒，可通过 `redis.expire.orderSubmitLock` 调整。
- 行为：短时间重复提交同一业务参数时直接返回“订单正在提交，请勿重复操作”。

库存锁定验证方式：

```text
1. 调用 /order/generateOrder 创建订单
   -> pms_sku_stock.lock_stock 增加
   -> 订单状态为 待付款(0)

2. 调用 /order/stockLockStatus/{orderId}
   -> 查看 stock、lockStock、availableStock

3. 调用 /order/paySuccess
   -> pms_sku_stock.stock 减少
   -> pms_sku_stock.lock_stock 释放
   -> 订单状态为 待发货(1)

4. 或调用 /order/cancelUserOrder
   -> pms_sku_stock.lock_stock 释放
   -> 订单状态为 已关闭(4)
```

RabbitMQ 超时关闭验证方式：

```text
1. 调用 /order/generateOrder 创建待付款订单
   -> 写入操作日志：提交订单，锁定SKU库存

2. 调用 /order/timeoutCancel/send?orderId={orderId}&delayTimes=5000
   -> 消息进入 mall.order.cancel.ttl 延迟队列
   -> 写入操作日志：发送订单超时关闭延迟消息

3. 等待 5 秒，消息过期进入 mall.order.cancel 消费队列
   -> CancelOrderReceiver 消费消息
   -> 订单状态从 待付款(0) 更新为 已关闭(4)
   -> 释放 pms_sku_stock.lock_stock
   -> 写入操作日志：MQ触发关闭并释放锁定库存

4. 调用 /order/timeoutCancel/trace/{orderId}
   -> 查看订单状态、队列名、库存锁定状态和完整操作日志
```

售后退款审核链路：

```text
待处理(0)
  -> 后台审核通过 -> 退货中(1)
  -> 后台审核拒绝 -> 已拒绝(3)

退货中(1)
  -> 后台确认收货完成 -> 已完成(2)
  -> 订单状态同步为 已关闭(4)
```

后台验证接口：

```text
POST /returnApply/audit/approve/{id}
POST /returnApply/audit/reject/{id}
POST /returnApply/audit/complete/{id}
GET  /order/{id}
```

验证重点：

- 不允许已完成、已拒绝的售后申请继续变更状态。
- 不允许从待处理直接变为已完成。
- 每次审核都会写入 `oms_order_operate_history`，后台订单详情可查看完整审计链路。

待完成：

- 补充客服 Agent 工具 API，使智能客服可以查询订单、物流、售后状态。

## 第三阶段：和智能客服项目联动

新增面向 Agent 的业务工具 API：

- 查询订单详情
- 查询物流轨迹
- 查询订单售后状态
- 创建退款申请
- 创建售后工单

已完成：

- 新增 `AgentOrderToolController`，统一暴露 `/agent-tools/**` 工具接口。
- 新增 `/agent-tools/orders/{orderSn}`，按订单编号查询订单详情、订单商品和操作日志。
- 新增 `/agent-tools/orders/id/{orderId}`，按订单 ID 查询订单详情。
- 新增 `/agent-tools/orders/{orderSn}/logistics`，查询物流公司、物流单号、发货时间和物流提示。
- 新增 `/agent-tools/orders/{orderSn}/return-applies`，查询订单关联售后申请。
- 新增 `/agent-tools/orders/{orderSn}/context`，一次性聚合订单、物流、售后上下文，适合作为 RAG 客服 Agent 的工具调用。
- 将 `/agent-tools/**` 加入 `mall-portal` 安全白名单，方便 Python AI 服务直接调用。

目标链路：

```text
RAG 智能客服
  -> Agent tool call
  -> 电商订单交易系统 /agent-tools API
  -> MySQL / Redis / RabbitMQ
  -> 返回订单、物流、退款结果
```

Agent 工具接口示例：

```text
GET /agent-tools/orders/{orderSn}
GET /agent-tools/orders/id/{orderId}
GET /agent-tools/orders/{orderSn}/logistics
GET /agent-tools/orders/{orderSn}/return-applies
GET /agent-tools/orders/{orderSn}/context
```

RAG 客服侧可将这些接口包装成工具：

```text
query_order(order_sn)
query_logistics(order_sn)
query_after_sales(order_sn)
query_order_context(order_sn)
```

## 第四阶段：简历可讲亮点

最终简历表达：

- 基于 Spring Boot + MyBatis 的电商订单交易系统，围绕商品、购物车、订单、库存、支付、优惠券和售后退款构建交易闭环。
- 使用 Redis 实现缓存、登录态和防重复提交，降低热点接口数据库压力。
- 使用 RabbitMQ 解耦订单超时关闭流程，支持订单创建后延迟检查与自动取消。
- 设计订单状态机，约束待支付、待发货、已发货、已完成、已关闭、退款中等状态流转。
- 实现支付回调幂等和库存回滚，避免重复通知导致订单状态异常。
- 暴露客服 Agent 工具 API，与 RAG 智能客服系统联动，实现订单查询、物流查询和退款申请。
