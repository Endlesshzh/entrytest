#!/bin/bash

# Setup test data in Redis
# Usage: ./setup-test-data.sh

echo "Setting up test data in Redis..."

# Check if redis-cli is available
if ! command -v redis-cli &> /dev/null; then
    echo "Error: redis-cli not found. Please install Redis client."
    exit 1
fi

# Test Redis connection
if ! redis-cli ping &> /dev/null; then
    echo "Error: Cannot connect to Redis. Please make sure Redis is running."
    exit 1
fi

echo "Connected to Redis successfully!"

# Set string values
echo "Setting string values..."
redis-cli SET mykey "Hello, Redis!"
redis-cli SET app:version "1.0.0"
redis-cli SET app:name "Redis Script Query System"

# Set hash values (user data)
echo "Setting user data..."
redis-cli HSET user:1001 name "张三" age "25" city "北京" email "zhangsan@example.com"
redis-cli HSET user:1002 name "李四" age "30" city "上海" email "lisi@example.com"
redis-cli HSET user:1003 name "王五" age "28" city "广州" email "wangwu@example.com"

# Set list values
echo "Setting list data..."
redis-cli LPUSH mylist "item1" "item2" "item3" "item4" "item5"
redis-cli LPUSH tasks "task1" "task2" "task3"

# Set set values
echo "Setting set data..."
redis-cli SADD myset "member1" "member2" "member3" "member4"
redis-cli SADD tags "redis" "groovy" "spring" "llm"

# Set sorted set values
echo "Setting sorted set data..."
redis-cli ZADD leaderboard 100 "player1" 200 "player2" 150 "player3"
redis-cli ZADD scores 85.5 "student1" 92.0 "student2" 78.5 "student3"

# Set with expiration
echo "Setting keys with expiration..."
redis-cli SETEX temp:session:123 3600 "session-data"
redis-cli SETEX cache:data 300 "cached-value"

echo ""
echo "Test data setup completed!"
echo ""
echo "You can now test the following scripts:"
echo ""
echo "1. Get string value:"
echo "   redis.get('mykey')"
echo ""
echo "2. Get user data:"
echo "   redis.hgetAll('user:1001')"
echo ""
echo "3. Get all users:"
echo "   redis.keys('user:*')"
echo ""
echo "4. Get list items:"
echo "   redis.lrange('mylist', 0, -1)"
echo ""
echo "5. Get set members:"
echo "   redis.smembers('tags')"
echo ""
