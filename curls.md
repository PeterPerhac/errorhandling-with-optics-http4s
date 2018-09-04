# Run through these commands top to bottom to test all functionality
curl -H "Content-Type: application/json" http://localhost:8080/users | jq .

curl -H "Content-Type: application/json" --data '{"username": "Peter", "age": 30}' http://localhost:8080/users | jq .
curl -H "Content-Type: application/json" --data '{"username": "Jesus", "age": 33}' http://localhost:8080/users | jq .

curl -XPUT -H "Content-Type: application/json" --data '{"age": 33}' http://localhost:8080/users/Peter | jq .

### create an already existing user
curl -vH "Content-Type: application/json" --data '{"username": "Jesus", "age": 33}' http://localhost:8080/users
### patch non-existing user
curl -vXPUT -H "Content-Type: application/json" --data '{"age": 33}' http://localhost:8080/users/Bob
### patch existing user with negative age
curl -vXPUT -H "Content-Type: application/json" --data '{"age": -33}' http://localhost:8080/users/Peter
