# Booklog

This is a re-write effort - replacement for an ancient command line app i used to use - Readinglog.

While I need a new app, I might as well get acquainted with http4s, doobie, circe, cats-effect and whatnot

The Postgres database can be created and pre-loaded with some data by running this simple command (provided you have docker running and docker-compose installed).

```bash
docker-compose up &
#later to stop/start
docker-compose stop
docker-compose start
```

To connect to local PostgreSQL database, use the `./connect-to-db.sh` script, using the default password "booklog".


to reload sample data from included script in data folder, provided you launched psql client from project root directory, use this command:

```
\i data/002-load-data.sql
```

exit psql by pressing Ctrl+D

date started working on this thing: 2018-05-12

