// 用户统计信息示例脚本
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
    averageAge: count > 0 ? totalAge / count : 0,
    totalAge: totalAge
]

