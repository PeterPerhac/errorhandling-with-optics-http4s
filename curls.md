# cURL commands

_note that I am using `jq` tool to format returned JSON when listing users. Install jq or adapt the relevant curl command._

`brew install jq` on OS X or `sudo apt-get install jq` on Linux

------

Run through these commands top to bottom to test all functionality:

```
curl -H "Content-Type: application/json" http://localhost:8080/users | jq .
```

```
curl -H "Content-Type: application/json" --data '{"username": "peter", "age": 33}' http://localhost:8080/users
curl -H "Content-Type: application/json" --data '{"username": "rick", "age": 33}' http://localhost:8080/users
```

```
curl -XPUT -H "Content-Type: application/json" --data '{"age": 50}' http://localhost:8080/users/rick
```

### create an already existing user

```
curl -vH "Content-Type: application/json" --data '{"username": "rick", "age": 33}' http://localhost:8080/users
```

### patch non-existing user

```
curl -vXPUT -H "Content-Type: application/json" --data '{"age": 33}' http://localhost:8080/users/bob
```

### patch existing user with negative age

```
curl -vXPUT -H "Content-Type: application/json" --data '{"age": -33}' http://localhost:8080/users/peter
```

