# service-poller
A simple service poller with a simple UI

## Running MySQL using Docker
In the root folder, create a directory called `mysql-data`:
```sh
mkdir mysql-data
```

Run MySQL using Docker:
```sh
docker run --name service-poller-mysql -v "$PWD/mysql-data":/var/lib/mysql -e MYSQL_ROOT_PASSWORD=password -d mysql:8.0.24
```

To connect:
```sh
docker exec -it service-poller-mysql mysql -uroot -p
```
