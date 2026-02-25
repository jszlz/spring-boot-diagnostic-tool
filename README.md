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

**jszlz**

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

---

# Spring Boot Diagnostic Tool

A lightweight diagnostic and architecture insight tool for Spring Boot applications. Without complex configuration, it can automatically analyze microservice dependencies, identify potential bottlenecks, and generate visual health reports.

## ✨ Core Features

- 🔍 **Automatic Dependency Discovery** - Automatically scan and identify external dependencies such as databases, Redis, HTTP services, message queues, etc.
- 📊 **Performance Monitoring** - Real-time collection of key metrics such as endpoint QPS, response time, P95/P99, error rate, etc.
- 🔗 **Dependency Topology** - Visual display of call relationships between services, databases, caches and other components
- 🚨 **Architecture Risk Identification** - Automatically detect single point of failure, circular dependencies, slow interfaces, high error rates and other risks
- 📈 **Performance Trend Analysis** - Use linear regression to analyze performance trends and detect performance degradation in time
- 📄 **Health Report Generation** - Generate comprehensive health reports in JSON and HTML formats
- 🌐 **REST API** - Provide complete REST API access to diagnostic data
- ⚡ **Low Performance Overhead** - Asynchronous data collection, memory protection mechanisms, minimal impact on application performance

## 🚀 Quick Start

### 1. Add Maven Dependency

```xml
<dependency>
    <groupId>com.diagnostic</groupId>
    <artifactId>diagnostic-autoconfigure</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Enable Diagnostic Functionality

Add `@EnableDiagnostics` annotation to your Spring Boot main class:

```java
@SpringBootApplication
@EnableDiagnostics
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3. Mark Endpoints for Monitoring

Add `@DiagnosticEndpoint` annotation to Controller methods:

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

### 4. Configuration (Optional)

Configure the diagnostic tool in `application.yml`:

```yaml
diagnostic:
  enabled: true
  monitor-all-endpoints: false  # Whether to monitor all endpoints
  slow-endpoint-threshold-ms: 1000  # Slow endpoint threshold (milliseconds)
  high-error-rate-threshold: 0.05  # High error rate threshold (5%)
  sampling-rate: 1.0  # Sampling rate (1.0 = 100%)
  data-retention-days: 7  # Data retention days
  api-enabled: true  # Whether to enable REST API
  report-output-path: ./diagnostic-reports  # Report output path
```

## 📡 REST API

The diagnostic tool provides the following REST API endpoints:

### Health Reports

```bash
# Get complete health report
GET /diagnostic/health

# Get report summary
GET /diagnostic/health/summary
```

### Dependency Topology

```bash
# Get dependency topology
GET /diagnostic/topology

# Get topology JSON data
GET /diagnostic/topology/json

# Get DOT format topology (for Graphviz)
GET /diagnostic/topology/dot
```

### Endpoint Statistics

```bash
# Get all endpoint statistics
GET /diagnostic/endpoints

# Get specific endpoint statistics
GET /diagnostic/endpoints/{name}

# Refresh endpoint statistics
POST /diagnostic/endpoints/{name}/refresh
```

### Architecture Risks

```bash
# Get all risks
GET /diagnostic/risks

# Get high severity risks
GET /diagnostic/risks/high
```

### Health Check

```bash
# Service status check
GET /diagnostic/status
```

## 📊 Health Report Example

The diagnostic tool generates health reports containing the following information:

- **Number of Monitored Endpoints** - Total number of currently monitored HTTP endpoints
- **Number of Identified Risks** - Total number of detected architectural risks
- **Number of External Dependencies** - Total number of discovered external dependencies
- **Number of Circular Dependencies** - Number of detected circular dependencies

### Architecture Risk Types

- 🔴 **Single Point of Failure** - Critical dependencies without redundancy backup
- 🔴 **Circular Dependencies** - Circular calls between services
- 🟡 **Performance Bottlenecks** - Endpoint response time exceeds threshold
- 🟡 **High Error Rate** - Endpoint error rate exceeds threshold
- 🟡 **Excessive Dependency** - Abnormally high call frequency of a dependency

### Endpoint Performance Metrics

- **Total Requests** - Total number of requests received by the endpoint
- **QPS** - Queries per second
- **Average Response Time** - Average response time of all requests
- **P95 Response Time** - 95% of request response times
- **P99 Response Time** - 99% of request response times
- **Error Rate** - Proportion of 4xx and 5xx responses
- **Performance Trend** - Rising, falling, or stable

## 🏗️ Project Structure

```
spring-boot-diagnostic-tool/
├── diagnostic-core/          # Core functionality module
│   ├── collector/           # Performance data collection
│   ├── analyzer/            # Health analysis and trend analysis
│   ├── storage/             # Data storage
│   ├── topology/            # Topology generation
│   ├── registry/            # Dependency registry
│   └── model/               # Data model
├── diagnostic-autoconfigure/ # Spring Boot auto-configuration
├── diagnostic-api/          # REST API
├── diagnostic-report/       # Report generation
└── example-app/             # Example application
```

## 🔧 Advanced Configuration

### Custom Sampling Rate

You can set different sampling rates for different endpoints:

```java
@DiagnosticEndpoint(name = "highTrafficEndpoint", samplingRate = 0.1)  // 10% sampling
public String highTrafficEndpoint() {
    return "response";
}
```

### Mark Critical Endpoints

Mark critical endpoints to highlight them in reports:

```java
@DiagnosticEndpoint(name = "criticalEndpoint", critical = true)
public String criticalEndpoint() {
    return "response";
}
```

### Custom Slow Endpoint Threshold

Set custom thresholds for specific endpoints:

```java
@DiagnosticEndpoint(name = "fastEndpoint", slowThresholdMs = 100)
public String fastEndpoint() {
    return "response";
}
```

## 📈 Performance Impact

The diagnostic tool uses the following strategies to minimize performance impact:

- ✅ **Asynchronous Data Collection** - All data collection operations are executed in background threads
- ✅ **Memory Protection** - Automatically monitor memory usage, reduce sampling rate when exceeding threshold
- ✅ **Data Volume Limitation** - Limit the amount of data retained in memory, automatically flush to disk
- ✅ **Graceful Degradation** - Automatically reduce sampling rate when memory pressure is high
- ✅ **ThreadPool Management** - Use independent thread pools, not affecting application threads

## 👨‍💻 Author

**jszlz**

## 🤝 Contribution

Welcome to submit Issues and Pull Requests!

## 📄 License

MIT License

## 🙏 Acknowledgments

Thanks to all the developers who contributed to this project!

---

# Spring Boot ダイアグノスティック ツール

Spring Bootアプリケーション向けの軽量な診断とアーキテクチャーインサイトツール。複雑な設定なしで、マイクロサービスの依存関係を自動的に分析し、潜在的なボトルネックを特定し、視覚的なヘルスレポートを生成することができます。

## ✨ コア機能

- 🔍 **自動依存関係発見** - データベース、Redis、HTTPサービス、メッセージキューなどの外部依存関係を自動的にスキャンして識別
- 📊 **パフォーマンスモニタリング** - エンドポイントのQPS、レスポンス時間、P95/P99、エラー率などの主要メトリクスをリアルタイムに収集
- 🔗 **依存関係トポロジー** - サービス、データベース、キャッシュなどのコンポーネント間の呼び出し関係を視覚的に表示
- 🚨 **アーキテクチャーリスク識別** - 単一障害点、循環依存、遅いインターフェース、高エラー率などのリスクを自動的に検出
- 📈 **パフォーマンストレンド分析** - 線形回帰を使用してパフォーマンストレンドを分析し、パフォーマンスの劣化を早期に発見
- 📄 **ヘルスレポート生成** - JSONおよびHTML形式の総合ヘルスレポートを生成
- 🌐 **REST API** - 診断データにアクセスするための完全なREST APIを提供
- ⚡ **低パフォーマンスオーバーヘッド** - 非同期データ収集、メモリ保護メカニズムにより、アプリケーションのパフォーマンスへの影響を最小限に抑える

## 🚀 クイックスタート

### 1. Maven依存関係を追加

```xml
<dependency>
    <groupId>com.diagnostic</groupId>
    <artifactId>diagnostic-autoconfigure</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 診断機能を有効にする

Spring Bootメインクラスに `@EnableDiagnostics` アノテーションを追加します：

```java
@SpringBootApplication
@EnableDiagnostics
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 3. モニタリングするエンドポイントをマークする

Controllerメソッドに `@DiagnosticEndpoint` アノテーションを追加します：

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

### 4. 設定（オプション）

`application.yml` で診断ツールを設定します：

```yaml
diagnostic:
  enabled: true
  monitor-all-endpoints: false  # すべてのエンドポイントをモニタリングするかどうか
  slow-endpoint-threshold-ms: 1000  # 遅いインターフェースのしきい値（ミリ秒）
  high-error-rate-threshold: 0.05  # 高エラー率のしきい値（5%）
  sampling-rate: 1.0  # サンプリングレート（1.0 = 100%）
  data-retention-days: 7  # データ保持日数
  api-enabled: true  # REST APIを有効にするかどうか
  report-output-path: ./diagnostic-reports  # レポート出力パス
```

## 📡 REST API

診断ツールは以下のREST APIエンドポイントを提供します：

### ヘルスレポート

```bash
# 完全なヘルスレポートを取得
GET /diagnostic/health

# レポートサマリーを取得
GET /diagnostic/health/summary
```

### 依存関係トポロジー

```bash
# 依存関係トポロジーを取得
GET /diagnostic/topology

# トポロジーJSONデータを取得
GET /diagnostic/topology/json

# DOT形式のトポロジーを取得（Graphviz用）
GET /diagnostic/topology/dot
```

### エンドポイント統計

```bash
# すべてのエンドポイント統計を取得
GET /diagnostic/endpoints

# 特定のエンドポイント統計を取得
GET /diagnostic/endpoints/{name}

# エンドポイント統計を更新
POST /diagnostic/endpoints/{name}/refresh
```

### アーキテクチャーリスク

```bash
# すべてのリスクを取得
GET /diagnostic/risks

# 高重大度のリスクを取得
GET /diagnostic/risks/high
```

### ヘルスチェック

```bash
# サービスステータスチェック
GET /diagnostic/status
```

## 📊 ヘルスレポートの例

診断ツールは以下の情報を含むヘルスレポートを生成します：

- **モニタリングされたエンドポイント数** - 現在モニタリングされているHTTPエンドポイントの総数
- **識別されたリスク数** - 検出されたアーキテクチャーリスクの総数
- **外部依存関係数** - 発見された外部依存関係の総数
- **循環依存関係数** - 検出された循環依存関係の数

### アーキテクチャーリスクの種類

- 🔴 **単一障害点** - 冗長バックアップのない重要な依存関係
- 🔴 **循環依存** - サービス間の循環呼び出し
- 🟡 **パフォーマンスボトルネック** - エンドポイントのレスポンス時間がしきい値を超える
- 🟡 **高エラー率** - エンドポイントのエラー率がしきい値を超える
- 🟡 **過度の依存** - 依存関係の呼び出し頻度が異常に高い

### エンドポイントパフォーマンスメトリクス

- **総リクエスト数** - エンドポイントが受け取った総リクエスト数
- **QPS** - 1秒あたりのクエリ数
- **平均レスポンス時間** - すべてのリクエストの平均レスポンス時間
- **P95レスポンス時間** - 95%のリクエストのレスポンス時間
- **P99レスポンス時間** - 99%のリクエストのレスポンス時間
- **エラー率** - 4xxおよび5xxレスポンスの割合
- **パフォーマンストレンド** - 上昇、下降、または安定

## 🏗️ プロジェクト構造

```
spring-boot-diagnostic-tool/
├── diagnostic-core/          # コア機能モジュール
│   ├── collector/           # パフォーマンスデータ収集
│   ├── analyzer/            # ヘルス分析とトレンド分析
│   ├── storage/             # データストレージ
│   ├── topology/            # トポロジー生成
│   ├── registry/            # 依存関係レジストリ
│   └── model/               # データモデル
├── diagnostic-autoconfigure/ # Spring Boot自動構成
├── diagnostic-api/          # REST API
├── diagnostic-report/       # レポート生成
└── example-app/             # サンプルアプリケーション
```

## 🔧 高度な設定

### カスタムサンプリングレート

異なるエンドポイントに異なるサンプリングレートを設定できます：

```java
@DiagnosticEndpoint(name = "highTrafficEndpoint", samplingRate = 0.1)  // 10%サンプリング
public String highTrafficEndpoint() {
    return "response";
}
```

### 重要なエンドポイントのマーキング

レポートで強調表示するために重要なエンドポイントをマークします：

```java
@DiagnosticEndpoint(name = "criticalEndpoint", critical = true)
public String criticalEndpoint() {
    return "response";
}
```

### カスタム遅いインターフェースのしきい値

特定のエンドポイントにカスタムしきい値を設定します：

```java
@DiagnosticEndpoint(name = "fastEndpoint", slowThresholdMs = 100)
public String fastEndpoint() {
    return "response";
}
```

## 📈 パフォーマンスへの影響

診断ツールは以下の戦略を採用してパフォーマンスへの影響を最小限に抑えます：

- ✅ **非同期データ収集** - すべてのデータ収集操作はバックグラウンドスレッドで実行されます
- ✅ **メモリ保護** - メモリ使用量を自動的にモニタリングし、しきい値を超えたときにサンプリングレートを低下させます
- ✅ **データ量の制限** - メモリに保持されるデータ量を制限し、自動的にディスクにフラッシュします
- ✅ **グレースフルデグレード** - メモリプレッシャーが高いときに自動的にサンプリングレートを低下させます
- ✅ **スレッドプール管理** - 独立したスレッドプールを使用し、アプリケーションスレッドに影響を与えません

## 👨‍💻 著者

**jszlz**

## 🤝 貢献

IssueとPull Requestを提出することを歓迎します！

## 📄 ライセンス

MIT License

## 🙏 謝辞

このプロジェクトに貢献したすべての開発者に感謝します！