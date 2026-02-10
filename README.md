# Spring Boot 诊断工具

面向Spring Boot应用的轻量级诊断与架构洞察工具。无需复杂配置，即可自动分析微服务依赖、识别潜在瓶颈，并生成可视化的健康报告。

## ✨ 核心特性

- 🔍 **自动依赖发现** - 自动扫描并识别数据库、Redis、HTTP服务、消息队列等外部依赖
- 📊 **性能监控** - 实时收集端点QPS、响应时间、P95/P99、错误率等关键指标
- 🔗 **依赖拓扑图** - 可视化展示服务、数据库、缓存等组件的调用关系
- 🚨 **架构风险识别** - 自动检测单点故障、循环依赖、慢接口、高错误率等风险
- 📈 **性能趋势分析** - 使用线性回归分析性能趋势，及时发现性能退化
- 📄 **健康报告生成** - 生成JSON和HTML格式的综合健康报告
- 🌐 **REST API** - 提供完整的REST API访问诊断数据
- ⚡ **低性能开销** - 异步数据收集，内存保护机制，对应用性能影响最小

## 🚀 快速开始

### 1. 添加Maven依赖

```xml
<dependency>
    <groupId>com.diagnostic</groupId>
    <artifactId>diagnostic-autoconfigure</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 启用诊断功能

在Spring Boot主类上添加 `@EnableDiagnostics` 注解：

```java
@SpringBootApplication
@EnableDiagnostics
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3. 标记需要监控的端点

在Controller方法上添加 `@DiagnosticEndpoint` 注解：

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    @DiagnosticEndpoint(name = "getUserById", slowThresholdMs = 500)
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

### 4. 配置（可选）

在 `application.yml` 中配置诊断工具：

```yaml
diagnostic:
  enabled: true
  monitor-all-endpoints: false  # 是否监控所有端点
  slow-endpoint-threshold-ms: 1000  # 慢接口阈值（毫秒）
  high-error-rate-threshold: 0.05  # 高错误率阈值（5%）
  sampling-rate: 1.0  # 采样率（1.0 = 100%）
  data-retention-days: 7  # 数据保留天数
  api-enabled: true  # 是否启用REST API
  report-output-path: ./diagnostic-reports  # 报告输出路径
```

## 📡 REST API

诊断工具提供以下REST API端点：

### 健康报告

```bash
# 获取完整健康报告
GET /diagnostic/health

# 获取报告摘要
GET /diagnostic/health/summary
```

### 依赖拓扑

```bash
# 获取依赖拓扑
GET /diagnostic/topology

# 获取拓扑JSON数据
GET /diagnostic/topology/json

# 获取DOT格式拓扑（用于Graphviz）
GET /diagnostic/topology/dot
```

### 端点统计

```bash
# 获取所有端点统计
GET /diagnostic/endpoints

# 获取特定端点统计
GET /diagnostic/endpoints/{name}

# 刷新端点统计
POST /diagnostic/endpoints/{name}/refresh
```

### 架构风险

```bash
# 获取所有风险
GET /diagnostic/risks

# 获取高严重性风险
GET /diagnostic/risks/high
```

### 健康检查

```bash
# 服务状态检查
GET /diagnostic/status
```

## 📊 健康报告示例

诊断工具会生成包含以下内容的健康报告：

- **监控端点数量** - 当前监控的HTTP端点总数
- **识别风险数量** - 检测到的架构风险总数
- **外部依赖数量** - 发现的外部依赖总数
- **循环依赖数量** - 检测到的循环依赖数量

### 架构风险类型

- 🔴 **单点故障** - 关键依赖没有冗余备份
- 🔴 **循环依赖** - 服务间存在循环调用
- 🟡 **性能瓶颈** - 端点响应时间超过阈值
- 🟡 **高错误率** - 端点错误率超过阈值
- 🟡 **过度依赖** - 某个依赖的调用频率异常高

### 端点性能指标

- **总请求数** - 端点接收的总请求数
- **QPS** - 每秒查询率
- **平均响应时间** - 所有请求的平均响应时间
- **P95响应时间** - 95%的请求响应时间
- **P99响应时间** - 99%的请求响应时间
- **错误率** - 4xx和5xx响应的占比
- **性能趋势** - 上升、下降或稳定

## 🏗️ 项目结构

```
spring-boot-diagnostic-tool/
├── diagnostic-core/          # 核心功能模块
│   ├── collector/           # 性能数据收集
│   ├── analyzer/            # 健康分析和趋势分析
│   ├── storage/             # 数据存储
│   ├── topology/            # 拓扑图生成
│   ├── registry/            # 依赖注册表
│   └── model/               # 数据模型
├── diagnostic-autoconfigure/ # Spring Boot自动配置
├── diagnostic-api/          # REST API
├── diagnostic-report/       # 报告生成
└── example-app/             # 示例应用
```

## 🔧 高级配置

### 自定义采样率

可以为不同端点设置不同的采样率：

```java
@DiagnosticEndpoint(name = "highTrafficEndpoint", samplingRate = 0.1)  // 10%采样
public String highTrafficEndpoint() {
    return "response";
}
```

### 标记关键端点

标记关键端点以在报告中突出显示：

```java
@DiagnosticEndpoint(name = "criticalEndpoint", critical = true)
public String criticalEndpoint() {
    return "response";
}
```

### 自定义慢接口阈值

为特定端点设置自定义阈值：

```java
@DiagnosticEndpoint(name = "fastEndpoint", slowThresholdMs = 100)
public String fastEndpoint() {
    return "response";
}
```

## 📈 性能影响

诊断工具采用以下策略最小化性能影响：

- ✅ **异步数据收集** - 所有数据收集操作在后台线程执行
- ✅ **内存保护** - 自动监控内存使用，超过阈值时降低采样率
- ✅ **数据量限制** - 限制内存中保留的数据量，自动刷新到磁盘
- ✅ **优雅降级** - 内存压力大时自动降低采样率
- ✅ **线程池管理** - 使用独立线程池，不影响应用线程

## 👨‍💻 作者

**剑神卓凌昭**

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！
