# Basic Redis Commands

Here are some basic Redis commands to get you started:

### Key-Value Commands:
- **SET**: Set the string value of a key.
  ```redis
  SET key value
  ```
- **GET**: Get the value of a key.
  ```redis
  GET key
  ```
- **DEL**: Delete a key.
  ```redis
  DEL key
  ```
- **EXISTS**: Check if a key exists.
  ```redis
  EXISTS key
  ```
- **EXPIRE**: Set a timeout on a key.
  ```redis
  EXPIRE key seconds
  ```
- **TTL**: Get the remaining time to live of a key.
  ```redis
  TTL key
  ```

### List Commands:
- **LPUSH**: Push a value onto a list from the left side.
  ```redis
  LPUSH list value
  ```
- **RPUSH**: Push a value onto a list from the right side.
  ```redis
  RPUSH list value
  ```
- **LPOP**: Pop a value from a list from the left side.
  ```redis
  LPOP list
  ```
- **RPOP**: Pop a value from a list from the right side.
  ```redis
  RPOP list
  ```
- **LRANGE**: Get a range of elements from a list.
  ```redis
  LRANGE list start stop
  ```

### Set Commands:
- **SADD**: Add a member to a set.
  ```redis
  SADD set member
  ```
- **SMEMBERS**: Get all the members in a set.
  ```redis
  SMEMBERS set
  ```
- **SREM**: Remove a member from a set.
  ```redis
  SREM set member
  ```
- **SISMEMBER**: Check if a set contains a member.
  ```redis
  SISMEMBER set member
  ```

### Hash Commands:
- **HSET**: Set the value of a field in a hash.
  ```redis
  HSET hash field value
  ```
- **HGET**: Get the value of a field in a hash.
  ```redis
  HGET hash field
  ```
- **HDEL**: Delete a field from a hash.
  ```redis
  HDEL hash field
  ```
- **HGETALL**: Get all the fields and values in a hash.
  ```redis
  HGETALL hash
  ```

### Sorted Set Commands:
- **ZADD**: Add a member to a sorted set, or update its score.
  ```redis
  ZADD sortedSet score member
  ```
- **ZRANGE**: Get a range of members in a sorted set, by index.
  ```redis
  ZRANGE sortedSet start stop
  ```
- **ZREM**: Remove a member from a sorted set.
  ```redis
  ZREM sortedSet member
  ```
- **ZSCORE**: Get the score of a member in a sorted set.
  ```redis
  ZSCORE sortedSet member
  ```

### Miscellaneous Commands:
- **PING**: Test the connection to the Redis server.
  ```redis
  PING
  ```
- **INFO**: Get information and statistics about the Redis server.
  ```redis
  INFO
  ```
- **FLUSHALL**: Remove all keys from all databases.
  ```redis
  FLUSHALL
  ```

These commands should give you a good start with Redis.