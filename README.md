# 系统监控Web面板

基于Vue前端框架和Spring Boot后端框架实现的深色主题系统监控Web面板，具备基础监控展示、交互与可视化能力。

## 技术选型

### 前端技术栈
- **HTML5 + CSS3**: 响应式布局和深色主题实现
- **JavaScript (ES6+)**: 核心交互逻辑和图表操作
- **jQuery 3.7.1**: DOM操作和AJAX请求
- **Bootstrap 5.3.2**: 响应式UI组件和布局
- **Chart.js 3.9.1**: 数据可视化图表库
- **SockJS 1.5.1 + STOMP 2.3.4**: WebSocket实时通信

### 后端技术栈
- **Spring Boot 4.0.1**: Java Web应用框架
- **Spring WebSocket**: 实时数据推送
- **Spring Data JPA**: 数据访问层
- **H2 Database**: 内存数据库（开发环境）
- **Maven**: 项目构建和依赖管理
- **Thymeleaf**: 服务端模板引擎

### 开发环境
- **Java 17**: 编程语言版本
- **Spring Boot 3.x**: 底层框架版本
- **Maven 3.x**: 构建工具版本

## AI使用方式与关键收获

### AI辅助开发流程
1. **需求分析**: 通过自然语言描述理解复杂的监控面板需求
2. **架构设计**: AI协助设计前后端分离架构和API接口
3. **代码生成**: 快速生成基础代码框架和核心功能
4. **问题诊断**: 通过日志分析快速定位Chart.js加载等关键问题
5. **代码优化**: AI提供最佳实践和性能优化建议

### 关键技术收获
1. **Chart.js集成方案**: 
   - 问题: webjars路径加载失败导致图表无法初始化
   - 解决方案: 多重备用加载源（webjars + CDN + 手动加载）
   - 收获: 实现了健壮的库加载机制，确保在各种环境下都能正常工作

2. **WebSocket实时通信**:
   - 实现了完整的STOMP协议通信
   - 支持多主题订阅和数据推送
   - 添加了连接状态监控和错误处理

3. **响应式布局设计**:
   - CSS Grid + Flexbox混合布局
   - 移动端适配和断点处理
   - 深色主题的完整实现

4. **数据可视化方案**:
   - 时序图表：多指标折线图
   - 分布图表：动态环形图
   - 实时数据更新和维度切换

## 项目结构

```
SystemMonitoring/
├── src/main/java/com/bub6le/systemmonitoring/
│   ├── SystemMonitoringApplication.java      # 主应用类
│   ├── config/                         # 配置类
│   │   ├── WebSocketConfig.java       # WebSocket配置
│   │   └── WebMvcConfig.java         # MVC配置
│   ├── controller/                      # 控制器层
│   │   ├── MainController.java        # 主页面控制器
│   │   ├── ApiController.java          # API控制器
│   │   └── WebSocketController.java   # WebSocket控制器
│   ├── model/                          # 数据模型
│   │   ├── SystemMetrics.java        # 系统指标模型
│   │   ├── Task.java                # 任务模型
│   │   └── Alert.java               # 告警模型
│   ├── repository/                     # 数据访问层
│   │   ├── SystemMetricsRepository.java
│   │   ├── TaskRepository.java
│   │   └── AlertRepository.java
│   └── service/                       # 业务逻辑层
│       ├── SystemMetricsService.java
│       ├── TaskService.java
│       ├── AlertService.java
│       └── DataInitializationService.java
├── src/main/resources/
│   ├── static/                        # 静态资源
│   │   ├── css/style.css           # 样式文件
│   │   └── js/monitoring.js      # 前端脚本
│   └── templates/
│       └── index.html              # 主页面模板
├── pom.xml                          # Maven配置文件
└── README.md                        # 项目说明文档
```

## 运行说明

### 环境要求
- Java 17+
- Maven 3.x
- 现代浏览器（Chrome, Firefox, Safari, Edge）

### 本地启动命令
```bash
# 克隆项目
git clone <repository-url>

# 进入项目目录
cd SystemMonitoring

# 使用Maven启动
mvn spring-boot:run

# 或者使用Maven Wrapper
./mvnw spring-boot:run
```

### 访问地址
- 主页面: http://localhost:8080
- H2控制台: http://localhost:8080/h2-console
- API端点: http://localhost:8080/api/

### 初始账号
系统使用H2内存数据库，无需额外配置。首次启动会自动创建表结构并初始化示例数据。

## 功能实现

### 1. 总体布局 ✅
- **响应式设计**: 支持≥1280px桌面宽度，1024px宽度降级兼容
- **顶部控制台**: 固定高度60px，包含全局操作和搜索
- **左侧边栏**: 固定宽度300px，包含任务管理和告警
- **右侧内容区**: 分为系统状态区（30%高度）和主可视化区（剩余空间）

### 2. 视觉主题 ✅
- **深色蓝黑系**: 完整实现CSS变量系统
```css
:root {
    --primary-bg: #0a0e27;
    --secondary-bg: #1a1d3e;
    --accent-blue: #00d4ff;
    --success-green: #00ff88;
    --warning-yellow: #ffb800;
    --error-red: #ff4757;
    --text-primary: #e6eef9;
    --text-secondary: #9aa5b1;
    --border-color: #2a2f55;
}
```

### 3. 功能点 ✅

#### 控制台（顶部）
- **全局操作按钮**: 启动/暂停数据流、刷新数据、切换主题
- **搜索与筛选**: 实时过滤服务器名/标签/区域
- **用户信息占位**: 预留用户信息显示区域

#### 任务管理面板（左侧边栏）
- **监控任务列表**: 显示任务名、目标集群、状态和进度
- **进度可视化**: 0-100%进度条，支持排队/运行中/失败/完成状态
- **异常告警记录**: 最近告警列表，包含时间、来源、严重级别和描述
- **状态统计**: 实时统计各状态任务数量

#### 系统状态监控区（右上）
- **核心指标聚合**: CPU、内存、磁盘、网络I/O、平均负载
- **负载均衡状态**: 流量分配分析和倾斜检测
- **健康度规则**: CPU>85%或内存>90%或负载>5判定不健康
- **服务健康指示**: 红/黄/绿三色实时状态指示
- **实时更新**: 每1-2秒自动刷新数据

#### 数据可视化中心（右侧主区域）
- **时序图表**: CPU和内存使用率的双线折线图
- **分布图表**: 按服务器/区域/服务类型的环形图
- **多维度切换**: 支持按服务器、区域、服务类型切换视图
- **历史回放**: 时间滑块选择器，支持5-15分钟历史数据回看
- **交互式图表**: 支持悬停提示、图例切换和数据点高亮

### 4. 工程与体验要求 ✅

#### 响应式布局
- **桌面优化**: 1280px+宽度下完美布局
- **平板兼容**: 1024px宽度下优雅降级
- **移动适配**: 基础响应式支持

#### 性能优化
- **惰性渲染**: 大数据量时的虚拟滚动支持
- **动画优化**: 图表更新使用'none'模式避免卡顿
- **缓存策略**: 合理的数据缓存和更新频率

#### 可靠性
- **状态保持**: 暂停/恢复不丢失数据状态
- **错误处理**: 完善的异常捕获和用户提示
- **空态处理**: 无数据时的友好提示

#### 可维护性
- **组件化设计**: 清晰的组件边界和职责分离
- **状态管理**: 局部状态与全局状态明确划分
- **代码规范**: 统一的命名规范和注释标准

#### 可访问性
- **键盘导航**: 完整的键盘操作支持
- **屏幕阅读器**: 语义化HTML和ARIA标签
- **颜色对比**: 深色主题下的WCAG 2.1 AA级对比度
- **焦点指示**: 清晰的焦点状态和跳转链接

## API接口

### 系统指标API
- `GET /api/metrics` - 获取所有系统指标
- `GET /api/metrics/recent?minutes=5` - 获取最近N分钟指标
- `GET /api/metrics/server/{serverName}` - 按服务器名获取指标
- `GET /api/metrics/health` - 获取系统健康状态
- `POST /api/metrics/generate` - 生成模拟指标数据

### 任务管理API
- `GET /api/tasks` - 获取所有任务
- `GET /api/tasks/status/{status}` - 按状态获取任务
- `POST /api/tasks` - 创建新任务
- `PUT /api/tasks/{id}/progress` - 更新任务进度
- `POST /api/tasks/generate` - 生成模拟任务

### 告警管理API
- `GET /api/alerts` - 获取所有告警
- `GET /api/alerts/unresolved` - 获取未解决告警
- `GET /api/alerts/severity/{severity}` - 按严重级别获取告警
- `POST /api/alerts` - 创建新告警
- `PUT /api/alerts/{id}/resolve` - 解决告警

### WebSocket端点
- `/ws` - WebSocket连接端点
- `/topic/metrics` - 系统指标数据推送
- `/topic/health` - 健康状态数据推送
- `/topic/tasks` - 任务数据推送
- `/topic/alerts` - 告警数据推送

## 数据模型

### SystemMetrics（系统指标）
```java
@Entity
public class SystemMetrics {
    private Long id;
    private String serverName;      // 服务器名
    private Double cpuUsage;       // CPU使用率
    private Double memoryUsage;    // 内存使用率
    private Double diskUsage;      // 磁盘使用率
    private Double networkIn;      // 网络入站
    private Double networkOut;     // 网络出站
    private Double loadAverage;    // 平均负载
    private String region;         // 区域
    private String serviceType;    // 服务类型
    private LocalDateTime timestamp; // 时间戳
}
```

### Task（任务）
```java
@Entity
public class Task {
    private Long id;
    private String taskName;        // 任务名
    private String targetCluster;     // 目标集群
    private TaskStatus status;       // 任务状态
    private Integer progress;        // 进度百分比
    private LocalDateTime createdTime; // 创建时间
    private LocalDateTime updatedTime; // 更新时间
    
    public enum TaskStatus {
        QUEUED,    // 排队中
        RUNNING,    // 运行中
        FAILED,     // 失败
        COMPLETED   // 完成
    }
}
```

### Alert（告警）
```java
@Entity
public class Alert {
    private Long id;
    private String source;           // 告警来源
    private AlertSeverity severity;  // 严重级别
    private String message;          // 告警消息
    private LocalDateTime timestamp; // 时间戳
    private Boolean resolved;        // 是否已解决
    
    public enum AlertSeverity {
        LOW,        // 低
        MEDIUM,     // 中
        HIGH,       // 高
        CRITICAL    // 严重
    }
}
```

## 部署说明

### 开发环境部署
```bash
# 打包应用
mvn clean package

# 运行应用
java -jar target/SystemMonitoring-0.0.1-SNAPSHOT.jar

# 指定配置文件
java -jar target/SystemMonitoring-0.0.1-SNAPSHOT.jar --spring.config.location=application.properties
```

### 生产环境部署
```bash
# 使用Docker部署
docker build -t system-monitoring .
docker run -p 8080:8080 system-monitoring

# 使用外部数据库
# 修改application.properties配置MySQL/PostgreSQL连接
spring.datasource.url=jdbc:mysql://localhost:3306/monitoring
spring.datasource.username=monitoring
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## 监控数据生成

### 模拟数据策略
系统内置了完整的模拟数据生成机制：
- **系统指标**: 随机生成CPU、内存、磁盘、网络和负载数据
- **任务管理**: 生成不同状态和进度的监控任务
- **告警系统**: 生成不同严重级别的告警信息
- **时间分布**: 数据均匀分布在最近时间范围内

### 数据初始化
应用启动时自动初始化：
- 20条系统指标记录（分布在最近5分钟）
- 10条不同状态的任务记录
- 5条不同级别的告警记录

## 故障排除

### 常见问题及解决方案

1. **图表不显示**
   - 检查Chart.js是否正确加载
   - 确认canvas元素是否存在
   - 查看浏览器控制台错误信息

2. **WebSocket连接失败**
   - 检查后端服务是否启动
   - 确认防火墙设置
   - 验证WebSocket配置

3. **数据不更新**
   - 检查数据流是否激活
   - 确认WebSocket连接状态
   - 查看网络请求是否成功

### 日志配置
```properties
# 应用日志级别
logging.level.com.bub6le.systemmonitoring=DEBUG

# WebSocket日志
logging.level.org.springframework.web.socket=DEBUG
```

## 扩展开发

### 添加新指标
1. 在`SystemMetrics`模型中添加新字段
2. 更新数据库Schema（Hibernate自动DDL）
3. 修改前端图表配置
4. 添加相应的API端点

### 集成外部系统
1. 实现自定义数据源
2. 添加认证和授权
3. 配置数据采集间隔
4. 实现告警通知机制

## 许可证

本项目采用MIT许可证，允许自由使用、修改和分发。

## 贡献指南

1. Fork本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -am 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 联系信息

- 项目维护者: [Your Name]
- 邮箱: [your.email@example.com]
- 项目地址: [https://github.com/yourusername/SystemMonitoring]

---

**注意**: 本项目仅用于学习和演示目的，生产环境使用请进行充分测试和安全评估。