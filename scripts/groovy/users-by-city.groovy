// 按城市分组用户示例脚本
// 将用户按城市分组并统计

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
            age: userData.age,
            email: userData.email
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

