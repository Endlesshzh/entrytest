# Groovy 脚本示例

这个目录包含了常用的 Groovy 脚本示例，可以在 Redis Script Query System 中使用。

## 如何使用这些脚本

### 方法1：通过 Web 界面

1. 启动应用后，访问 http://localhost:8080
2. 打开任意一个 `.groovy` 文件，复制其内容
3. 粘贴到 Web 界面的脚本编辑器中
4. 点击"试运行"或"执行脚本"按钮

### 方法2：通过 REST API

```bash
# 读取脚本文件内容
SCRIPT=$(cat scripts/groovy/get-user-info.groovy)

# 通过API执行
curl -X POST http://localhost:8080/api/script/execute \
  -H "Content-Type: application/json" \
  -d "{
    \"script\": \"$SCRIPT\",
    \"scriptName\": \"get-user-info\",
    \"testRun\": false
  }"
```

## 脚本列表

### 基础操作

- **get-simple-value.groovy** - 获取简单的字符串值
- **get-user-info.groovy** - 获取单个用户信息
- **get-list-items.groovy** - 获取List类型数据
- **get-set-members.groovy** - 获取Set类型数据
- **get-leaderboard.groovy** - 获取排行榜数据

### 高级查询

- **get-all-users.groovy** - 获取所有用户信息
- **filter-users-by-age.groovy** - 按年龄过滤用户
- **user-statistics.groovy** - 用户统计信息
- **users-by-city.groovy** - 按城市分组用户

## 脚本编写规范

1. **必须使用 return 语句返回结果**
2. **使用 redis 对象进行 Redis 操作**
3. **避免使用危险操作**（如 System.exit, 文件操作等）
4. **控制循环次数**，避免性能问题
5. **添加注释**说明脚本功能

## 可用的 Redis 操作

在脚本中可以使用以下 Redis 操作：

- `redis.get(key)` - 获取字符串值
- `redis.set(key, value)` - 设置字符串值
- `redis.hget(key, field)` - 获取Hash字段值
- `redis.hgetAll(key)` - 获取Hash所有字段
- `redis.hset(key, field, value)` - 设置Hash字段
- `redis.keys(pattern)` - 查询键（生产环境慎用）
- `redis.lrange(key, start, end)` - 获取List范围
- `redis.smembers(key)` - 获取Set所有成员
- `redis.zrange(key, start, end)` - 获取ZSet范围
- `redis.exists(key)` - 检查键是否存在
- `redis.ttl(key)` - 获取键的过期时间

## 注意事项

⚠️ **重要提示**：

1. 这些脚本需要在 Redis 中有对应的测试数据才能正常运行
2. 运行 `scripts/setup-test-data.sh` 来准备测试数据
3. 在生产环境中，避免使用 `redis.keys('*')`，建议使用 SCAN
4. 脚本执行有超时限制（默认5秒），确保脚本能在超时时间内完成

## 更多示例

查看 `EXAMPLES.md` 文件获取更多详细的脚本示例和最佳实践。

