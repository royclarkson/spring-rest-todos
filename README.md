# Spring REST Todos

A simple todo list example built with Spring

## Build and Run

```sh
./gradlew clean build bootRun
```

## Test

The following curl commands can be used to test the API.

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
```

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

## Run the Web Client

### Setup

```sh
npm install -g bower
cd public
bower install
```

#### Temporary workaround for bower moduleType and RaveJS support

There is [an open PR](https://github.com/cujojs/rest/pull/58) to add `moduleType` to rest.js to support RaveJS.  Until that's merged, you need to add it manually to `public/bower_components/rest/bower.json` after running `bower install`:

```json
  moduleType: ["amd", "node"]
```

### Open in Browser

Go to http://localhost:8080/index.html