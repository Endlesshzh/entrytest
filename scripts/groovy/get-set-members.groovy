// 获取Set类型数据示例脚本
// 获取集合的所有成员

def members = redis.smembers('tags')
return members.collect { it.toString() }

