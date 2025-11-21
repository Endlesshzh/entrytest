// 按年龄过滤用户示例脚本
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

