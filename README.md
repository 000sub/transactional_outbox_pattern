# transactional_outbox_pattern

## mongosh 접속 및 데이터 확인
```
mongosh admin -u root -p root
db.testCollection.find()
```

## kafka topic 초기화 (모든 데이터 제거)
```
/opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9094 --delete --topic test-topic
```
