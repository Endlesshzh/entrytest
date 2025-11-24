// 获取用户信息示例脚本
// 用法：在Web界面中复制此脚本内容，或通过API调用

// 获取单个用户的所有信息
def userData = redis.hgetAll('user:1001')
return userData

