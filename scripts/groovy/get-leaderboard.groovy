// 获取排行榜示例脚本
// 获取Sorted Set的前10名

def topPlayers = redis.zrange('leaderboard', 0, 9)
return topPlayers

