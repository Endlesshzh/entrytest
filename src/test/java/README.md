# 测试类说明文档

## 📋 测试类概览

本项目包含以下测试类，用于测试所有REST API接口：

### 1. ScriptControllerTest
**位置**: `src/test/java/org/example/controller/ScriptControllerTest.java`

**测试范围**: 脚本操作相关的所有接口

**测试用例**:
- ✅ 执行脚本接口 - 成功场景
- ✅ 执行脚本接口 - 失败场景
- ✅ 执行脚本接口 - 异常场景
- ✅ 试运行脚本接口
- ✅ 分析脚本接口 - 成功场景
- ✅ 分析脚本接口 - 失败场景
- ✅ 分析脚本接口 - 异常场景
- ✅ 健康检查接口
- ✅ 执行脚本接口 - 空脚本
- ✅ 执行脚本接口 - 复杂脚本
- ✅ 分析脚本接口 - 包含安全问题的脚本

**覆盖的接口**:
- `POST /api/script/execute` - 执行脚本
- `POST /api/script/test` - 试运行脚本
- `POST /api/script/analyze` - 分析脚本
- `GET /api/script/health` - 健康检查

---

### 2. DataControllerTest
**位置**: `src/test/java/org/example/controller/DataControllerTest.java`

**测试范围**: 数据管理相关的所有接口

**测试用例**:
- ✅ 生成测试数据接口 - 使用默认参数
- ✅ 生成测试数据接口 - 使用自定义参数
- ✅ 生成测试数据接口 - 异常场景
- ✅ 清空测试数据接口 - 成功场景
- ✅ 清空测试数据接口 - 异常场景
- ✅ 获取数据统计接口 - 成功场景
- ✅ 获取数据统计接口 - 空数据场景
- ✅ 获取数据统计接口 - 异常场景
- ✅ 生成测试数据接口 - 边界值测试

**覆盖的接口**:
- `POST /api/data/generate` - 生成测试数据
- `DELETE /api/data/clear` - 清空测试数据
- `GET /api/data/statistics` - 获取数据统计

---

### 3. WebControllerTest
**位置**: `src/test/java/org/example/controller/WebControllerTest.java`

**测试范围**: Web页面相关的接口

**测试用例**:
- ✅ 获取主页面接口
- ✅ 主页面返回正确的视图名称
- ✅ 根路径重定向

**覆盖的接口**:
- `GET /` - 返回主页面

---

### 4. ControllerIntegrationTest
**位置**: `src/test/java/org/example/controller/ControllerIntegrationTest.java`

**测试范围**: 跨Controller的集成测试场景

**测试用例**:
- ✅ 完整的脚本执行流程（生成数据 → 执行脚本 → 分析脚本 → 获取统计 → 清理数据）
- ✅ 试运行脚本流程
- ✅ 健康检查
- ✅ Web页面访问

**注意**: 此测试需要Redis服务运行，使用`@ActiveProfiles("test")`激活测试配置

---

### 5. TestUtils
**位置**: `src/test/java/org/example/TestUtils.java`

**说明**: 测试工具类，提供测试中常用的辅助方法

**提供的方法**:
- `createSimpleExecutionRequest(String script)` - 创建简单的脚本执行请求
- `createTestRunRequest(String script)` - 创建试运行脚本请求
- `createAnalysisRequest(String script)` - 创建脚本分析请求
- `getSimpleQueryScript()` - 获取示例脚本 - 简单查询
- `getHashQueryScript()` - 获取示例脚本 - Hash查询
- `getKeysQueryScript()` - 获取示例脚本 - 键列表查询
- `getComplexQueryScript()` - 获取示例脚本 - 复杂查询
- `getUnsafeScript()` - 获取包含安全问题的脚本

---

## 🧪 运行测试

### 运行所有测试
```bash
mvn test
```

### 运行特定测试类
```bash
mvn test -Dtest=ScriptControllerTest
mvn test -Dtest=DataControllerTest
mvn test -Dtest=WebControllerTest
mvn test -Dtest=ControllerIntegrationTest
```

### 运行特定测试方法
```bash
mvn test -Dtest=ScriptControllerTest#testExecuteScript_Success
```

---

## 📝 测试配置

### 测试配置文件
**位置**: `src/test/resources/application-test.yml`

**配置说明**:
- 使用独立的Redis数据库（database: 15）避免影响生产数据
- 使用随机端口（port: 0）避免端口冲突
- 简化了LLM配置，使用较短的超时时间

### 测试依赖
测试使用以下Mock框架：
- **Mockito**: 用于Mock服务层
- **Spring Boot Test**: 提供MockMvc用于测试Controller
- **JUnit 5**: 测试框架

---

## ✅ 测试覆盖率

### 接口覆盖率
- ✅ ScriptController: 100% (4/4 接口)
- ✅ DataController: 100% (3/3 接口)
- ✅ WebController: 100% (1/1 接口)

### 场景覆盖率
- ✅ 成功场景
- ✅ 失败场景
- ✅ 异常场景
- ✅ 边界值测试
- ✅ 集成测试

---

## 🔍 测试最佳实践

1. **单元测试**: 使用`@WebMvcTest`只加载Controller层，Mock服务层
2. **集成测试**: 使用`@SpringBootTest`加载完整应用上下文
3. **测试隔离**: 每个测试方法独立，不依赖其他测试的执行顺序
4. **Mock使用**: 使用`@MockBean`Mock依赖的服务，避免真实调用
5. **断言完整**: 验证响应状态码、内容类型、JSON结构等

---

## 📚 相关文档

- API接口文档: `API_DOCUMENTATION.md`
- 需求评估文档: `REQUIREMENTS_ASSESSMENT.md`
- 项目架构文档: `ARCHITECTURE.md`

