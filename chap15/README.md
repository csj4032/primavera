## chap14

### Step
1. Configuration Server Start
2. Account Server Start
3. Product Server Start
4. Order Server Start
5. Front Server Start

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
