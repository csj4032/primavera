## chap14

### WebFlux

### Docker For Redis

#### Downloading an Image

```
docker search redis

docker login --username [username]

docker pull redis:latest
```

#### Creating a Container

```
docker run --name redis -d -p 6379:6379 redis

docker ps

docker exec -it redis bash

```
