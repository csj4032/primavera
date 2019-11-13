## chap14 : account

### OrderServer - WebFlux

### Redis

```shell script
$> redis-cli
$> keys *
$> hmset USER:0 id 1 name genius createDate 2019
$> hmsetnx USER:0 id 0
$> hmget USER:0 id name createDate
$> hlen USER:0
$> hdel USER:0 device
$> hgetall USER:0
```