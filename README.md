main
====

this is a tool repo for delete multi keys in redis cluster

otherwise redis cluster will throw 

	CROSSSLOT Keys in request don't hash to the same slot

1、download the tool jar
<https://github.com/wx5223/redis-cluster-del/releases/download/1.0/redis-cluster-del.jar>

2、install java8

3、exec the jar with 3 params

	java -jar redis-cluster-del.jar "127.0.0.1" "6379" "keys*"