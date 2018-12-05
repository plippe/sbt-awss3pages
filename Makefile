.PHONY: clean build test release
.DEFAULT_GOAL := build

clean:
	sbt ^clean

build:
	sbt ^compile

test:
	sbt ^test
	cd examples/foobar && sbt compile

release:
	sbt ^publish
