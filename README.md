spring-rest-json-patch
======================

Request the list of todos:

```sh
curl -X "GET" -v localhost:8080/todos
```

Add a new todo:

```sh
curl -X "POST" -v localhost:8080/todos -H "Content-Type: application/json" -d '{"description":"A Todo","complete":false}'
```

Modify an existing todo:

```sh
curl -X "PUT" -v localhost:8080/todos/0 -H "Content-Type: application/json" -d '{"description":"Modified Todo","complete":false}'

Delete a todo:

```sh
curl -X "DELETE" -v localhost:8080/todos/0
```

Apply a JSON PATCH to the todo list:

```sh
curl -X "PATCH" -v localhost:8080/todos -H "Content-Type: application/json" -d '[{"op":"replace","path":"/0/description","value":"go go go!"}]'
```

Generate a JSON PATCH from a modified todo list:

```sh
curl -X "POST" -v localhost:8080/todos/diff -H "Content-Type: application/json" -d '[{"description":"go go go!","complete":false},{"description":"b","complete":false}]'
```
