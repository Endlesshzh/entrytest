// 获取所有用户信息示例脚本
// 查询所有以 user: 开头的键，并获取每个用户的详细信息

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

