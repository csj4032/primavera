## chap15

### POI Excel database to file

### POI Excel file to database

### PostgreSQL

```
docker pull postgres
docker run --name postgres -d -p 5432:5432 -e POSTGRES_PASSWORD=primavera -e POSTGRES_USER=primavera postgres
docker exec -it postgres bash

root@598a501b7d8e:/# psql -U postgres
psql (11.5 (Debian 11.5-1.pgdg90+1))
Type "help" for help.

postgres=# CREATE DATABASE primavera;
CREATE DATABASE

postgres=# \q

```