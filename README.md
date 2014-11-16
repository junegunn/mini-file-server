# mini-file-server

A simple file server written in Clojure and ClojureScript.

## Build

```
lein uberjar
```

## Run

```
java -jar mini-file-server-0.1.0-standalone.jar ~/data-dir 3000
```

## API

```
curl -XPOST -F file=@fairy-tale.pdf -F group=books/2014 localhost:3000
  # {"url":"http://localhost:3000/books/2014/fairy-tale.pdf"}
```

## TODO

- Tests
- Authentication and authorization

## License

MIT

