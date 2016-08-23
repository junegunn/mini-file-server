all:
	lein bin

ring:
	lein ring server-headless

repl:
	lein repl :connect localhost:9999

clean:
	lein clean

.PHONY: all ring repl clean
