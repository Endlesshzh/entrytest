# Script Examples

这里提供了一些常用的Groovy脚本示例，用于查询Redis数据。

## 基础操作

### 1. 获取单个键值

```groovy
// 获取字符串值
def value = redis.get('mykey')
return value
```

### 2. 设置键值

```groovy
// 设置字符串值
redis.set('mykey', 'Hello, World!')
return "Value set successfully"
```

### 3. 检查键是否存在

```groovy
// 检查键是否存在
def exists = redis.exists('mykey')
return exists ? "Key exists" : "Key does not exist"
```

### 4. 获取键的过期时间

```groovy
// 获取TTL
def ttl = redis.ttl('mykey')
return "TTL: ${ttl} seconds"
```

## Hash操作

### 5. 获取Hash单个字段

```groovy
// 获取Hash字段值
def name = redis.hget('user:1001', 'name')
return name
```

### 6. 获取Hash所有字段

```groovy
// 获取Hash所有字段
def userData = redis.hgetAll('user:1001')
return userData
```

### 7. 设置Hash字段

```groovy
// 设置Hash字段
redis.hset('user:1001', 'age', '26')
return "Field updated"
```

### 8. 获取多个用户信息

```groovy
// 获取所有用户的详细信息
def userKeys = redis.keys('user:*')
def users = []

userKeys.each { key ->
    def userData = redis.hgetAll(key)
    if (userData) {
        userData.put('key', key)
        users.add(userData)
    }
}

return users
```

## List操作

### 9. 获取List所有元素

```groovy
// 获取List所有元素
def items = redis.lrange('mylist', 0, -1)
return items
```

### 10. 获取List部分元素

```groovy
// 获取List前3个元素
def items = redis.lrange('mylist', 0, 2)
return items
```

## Set操作

### 11. 获取Set所有成员

```groovy
// 获取Set所有成员
def members = redis.smembers('myset')
return members
```

### 12. 获取多个Set的成员

```groovy
// 获取所有标签
def tags = redis.smembers('tags')
return tags.collect { it.toString() }
```

## Sorted Set操作

### 13. 获取ZSet范围

```groovy
// 获取排行榜前10名
def topPlayers = redis.zrange('leaderboard', 0, 9)
return topPlayers
```

### 14. 获取ZSet倒序范围

```groovy
// 获取分数最高的前5名
def topScores = redis.zrange('scores', -5, -1)
return topScores.reverse()
```

## 高级查询

### 15. 条件过滤

```groovy
// 获取所有年龄大于25的用户
def userKeys = redis.keys('user:*')
def filteredUsers = []

userKeys.each { key ->
    def userData = redis.hgetAll(key)
    if (userData && userData.age) {
        def age = userData.age as Integer
        if (age > 25) {
            userData.put('key', key)
            filteredUsers.add(userData)
        }
    }
}

return filteredUsers
```

### 16. 数据聚合

```groovy
// 统计用户数量和平均年龄
def userKeys = redis.keys('user:*')
def totalAge = 0
def count = 0

userKeys.each { key ->
    def userData = redis.hgetAll(key)
    if (userData && userData.age) {
        totalAge += userData.age as Integer
        count++
    }
}

return [
    totalUsers: count,
    averageAge: count > 0 ? totalAge / count : 0
]
```

### 17. 数据转换

```groovy
// 将用户数据转换为特定格式
def userKeys = redis.keys('user:*')
def formattedUsers = []

userKeys.each { key ->
    def userData = redis.hgetAll(key)
    if (userData) {
        formattedUsers.add([
            id: key.split(':')[1],
            fullName: userData.name,
            age: userData.age as Integer,
            location: userData.city
        ])
    }
}

return formattedUsers
```

### 18. 多数据源组合

```groovy
// 组合多个数据源
def appName = redis.get('app:name')
def appVersion = redis.get('app:version')
def userCount = redis.keys('user:*').size()
def tags = redis.smembers('tags')

return [
    application: [
        name: appName,
        version: appVersion
    ],
    statistics: [
        totalUsers: userCount,
        tags: tags
    ]
]
```

### 19. 数据验证

```groovy
// 验证数据完整性
def userKeys = redis.keys('user:*')
def invalidUsers = []

userKeys.each { key ->
    def userData = redis.hgetAll(key)
    if (userData) {
        def issues = []

        if (!userData.name) issues.add('Missing name')
        if (!userData.age) issues.add('Missing age')
        if (!userData.email) issues.add('Missing email')

        if (issues.size() > 0) {
            invalidUsers.add([
                key: key,
                issues: issues
            ])
        }
    }
}

return invalidUsers.size() > 0 ? invalidUsers : "All users are valid"
```

### 20. 复杂业务逻辑

```groovy
// 根据城市分组用户
def userKeys = redis.keys('user:*')
def usersByCity = [:]

userKeys.each { key ->
    def userData = redis.hgetAll(key)
    if (userData && userData.city) {
        def city = userData.city
        if (!usersByCity[city]) {
            usersByCity[city] = []
        }
        usersByCity[city].add([
            name: userData.name,
            age: userData.age
        ])
    }
}

// 计算每个城市的统计信息
def result = [:]
usersByCity.each { city, users ->
    def totalAge = users.sum { (it.age as Integer) }
    result[city] = [
        count: users.size(),
        averageAge: totalAge / users.size(),
        users: users
    ]
}

return result
```

## 性能优化示例

### 21. 使用批量操作（避免循环中的单次操作）

```groovy
// 不推荐：循环中多次调用
// def keys = redis.keys('user:*')
// keys.each { key -> redis.get(key) }

// 推荐：尽量减少Redis调用次数
def userKeys = redis.keys('user:*')
def users = userKeys.collect { key ->
    redis.hgetAll(key)
}
return users
```

### 22. 限制结果数量

```groovy
// 限制返回结果数量，避免内存溢出
def userKeys = redis.keys('user:*')
def limit = 10
def limitedUsers = userKeys.take(limit).collect { key ->
    redis.hgetAll(key)
}

return [
    total: userKeys.size(),
    returned: limitedUsers.size(),
    users: limitedUsers
]
```

## 错误处理示例

### 23. 安全的数据访问

```groovy
// 安全地访问可能不存在的数据
def key = 'user:9999'
def userData = redis.hgetAll(key)

if (userData && userData.size() > 0) {
    return userData
} else {
    return [error: "User not found", key: key]
}
```

### 24. 数据类型转换

```groovy
// 安全的类型转换
def userKeys = redis.keys('user:*')
def users = []

userKeys.each { key ->
    def userData = redis.hgetAll(key)
    if (userData) {
        try {
            users.add([
                name: userData.name ?: 'Unknown',
                age: userData.age ? (userData.age as Integer) : 0,
                city: userData.city ?: 'Unknown'
            ])
        } catch (Exception e) {
            log.warn("Error processing user ${key}: ${e.message}")
        }
    }
}

return users
```

## 注意事项

1. **避免使用KEYS命令在生产环境**: `redis.keys('*')` 会阻塞Redis，建议使用SCAN
2. **控制循环次数**: 避免在脚本中使用大量循环
3. **设置合理的超时**: 确保脚本能在超时时间内完成
4. **数据验证**: 始终验证从Redis获取的数据
5. **错误处理**: 使用try-catch处理可能的异常
6. **性能考虑**: 减少Redis调用次数，批量操作优于循环操作

## 测试建议

在执行脚本前，建议：
1. 先使用"试运行"功能测试脚本
2. 使用"LLM分析"检查脚本安全性
3. 从小数据集开始测试
4. 逐步增加复杂度
