// 获取List类型数据示例脚本
// 获取列表的所有元素

def items = redis.lrange('mylist', 0, -1)
return items

