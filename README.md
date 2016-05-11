# mini-file-server

A simple file server written in Clojure and ClojureScript.

## Build

```
lein bin
```

## Run

```sh
# Using executable
target/mini-file-server ~/data-dir 3000

# Using Uberjar (optionally with extra system properties)
java -Djava.io.tmpdir=. -jar mini-file-server-0.1.1-standalone.jar ~/data-dir 3000
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

