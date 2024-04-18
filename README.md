# Kafka

<br><br>

## 개념 정리
![image](https://github.com/Young-Geun/Kafka/assets/27760576/e5d88001-3adf-4b47-954a-eae9f5e125da)
### Topic
- Topic은 파티션으로 구성된 일련의 로그 파일이다.
- RDBMS의 테이블과 유사항 기능이다.
- Key와 Value 기반의 메시지 구조이며, Value는 어떠한 타입도 가능하다.
- 시간의 흐름에 따라 메시지가 순차적으로 물리적인 파일에 써진다.
- 파티션은 Kafka의 병렬 성능과 가용성 기능의 핵심 요소이다.
### Producer와 Consumer
- Producer
  - 토픽에 메시지를 보내는 역할을 한다.
  - 성능 / 로드밸런싱 / 가용성 / 업무 장합성 등을 고려하여 어떤 브로커의 파티션으로 메시지를 보내야할지 전략적으로 결정한다.
- Consumer
  - 토픽에서 메시지를 읽는 역할을 한다.
  - 여러 개의 Consumer들로 구성될 경우, 어떤 브로커의 파티션에서 메시지를 읽어들일지 전략적으로 결정한다.
- Consumer Group과 Consumer
  - 모든 Consumer들은 단 하나의 Consumer Group에 소속되어야 하며, Consumer Group은 1개 이상의 Consumer를 가질 수 있다.
  - Consumer Group 내에 Consumer 변화가 있을 시 마다 파티션과 Consumer의 조합을 변경하는 Rebalancing이 발생한다.
### 메시지
- 메시지는 Producer를 통해 전송 시 파티셔너를 통해 토픽의 어떤 파티션으로 전송되어야 할 지 미리 결정된다.
- Topic이 여러 개의 파티션을 가질 때, 메시지의 전송 순서가 보장되지 않은 채로 Consumer에서 읽혀질 수 있다. (파티션 내에서의 순서는 보장된다.)
- Key값을 가지지 않는 경우, 라운드 로빈, 스티키 파티션 등의 파티션 전략 등이 선택되어 파티션 별로 메시지가 전송될 수 있다.
- Key값을 가지는 경우, 특정 파티션으로 고정되어 전송된다.
### Producer와 acks 설정
- Producer는 해당 Topic의 Partition의 Leader Broker에게만 메시지를 보낸다.
- acks 설정에 따른 send 방식은 아래와 같다.
  - acks 0
    - ![image](https://github.com/Young-Geun/Kafka/assets/27760576/02bc14b1-7e88-41fa-8ee2-b8eac07879c0)
    - Producer는 Leader broker가 메시지 A를 정상적으로 받았는지에 대한 Ack 메시지를 받지 않고 다음 메시지인 메시지 B를 바로 전송한다.
    - 메시지가 제대로 전송되었는지 브로커로부터 확인을 받지 않기 때문에 메시지가 브로커에 기록되지 않더라도 재전송하지 않는다.
    - 메시지 손실의 우려가 가장 크지만 가장 빠르게 전송할 수 있다.
  - acks 1
    - ![image](https://github.com/Young-Geun/Kafka/assets/27760576/8caf825e-2e35-4f17-95e8-6b616df672d9)
    - Producer는 Leader broker가 메시지 A를 정상적으로 받았는지에 대한 Ack 메시지를 받은 후 다음 메시지인 메시지 B를 바로 전송. 만약 오류 메시지를 브로커로부터 받으면 메시지 A를 재전송한다.
    - 메시지 A가 모든 Replicator에 완벽하게 복사되었는지의 여부는 확인하지 않고 메시지 B를 전송한다.
    - 만약 Leader가 메시지를 복제 중에 다운될 경우 다음 Leader가 될 브로커에는 메시지가 없을 수 있기 때문에 메시지를 소실할 우려가 있다.
  - acks all(=acks -1)
    - ![image](https://github.com/Young-Geun/Kafka/assets/27760576/46f12045-8d8e-4153-81f6-69ff0234ce3c)
    - Producer는 Leader broker가 메시지 A를 정상적으로 받은 뒤 min.insync.replicas 개수 만큼의 Replicator에 복제를 수행한 뒤에 보내는 Ack 메시지를 받은 후 다음 메시지인 메시지 B를 바로 전송하고, 만약 오류 메시지를 브로커로부터 받으면 메시지 A를 재전송한다.
    - 메시지 A가 모든 Replicator에 완벽하게 복사되었는지의 여부까지 확인 후에 메시지 B를 전송한다.
    - 메시지 손실이 되지 않도록 모든 장애 상황을 감안한 전송 모드이지만 Ack를 오래 기다려야 하므로 상대적으로 전송속도가 느리다.
### Producer와 Batch
- ![image](https://github.com/Young-Geun/Kafka/assets/27760576/c6ccaf5c-85d5-4780-9fcc-26b2dd8d1fbd)
- 메시지마다 매번 네트워크를 통해 전달하는 것은 비효율적이기 때문에 Producer는 지정된만큼 메시지를 저장했다가 한번에 브로커로 전달한다.
- 배치를 책임지는 프로듀서 내부의 Record Accumulator(RA)는 각 토픽 파티션에 대응하는 배치 Queue를 구성하고 메시지들을 Record Batch 형태로 묶어 Queue에 저장한다.
- 각 배치 Queue에 저장된 레코드 배치들은 때가 되면 각 각 브로커에 전달된다.
- 주요 옵션
  - linger.ms : Sender Thread로 메시지를 보내 기전 배치로 메시지를 만들어서 보내기 위한 최대 대기 시간
  - buffer.memory : Record accumulator의 전체 메모리 사이즈
  - batch.size : 단일 배치의 사이즈


<br><hr><br>

## 환경 구축
### 우분투 설치
- https://velog.io/@suzu11/Mac-M1-%EC%97%90%EC%84%9C-%EC%9A%B0%EB%B6%84%ED%88%AC-%EC%84%A4%EC%B9%98%ED%95%98%EA%B8%B0-UTM

### 우분투 SSH 설정
- 설치
  - sudo apt-get install openssh-server
- 확인
  - sudo systemctl status sshd
- 접속
  - ssh 사용자ID@서버IP (Ex. ssh choi@192.168.64.2)

### Java 설치
- sudo apt-get install openjdk-11-jdk

### Kafka 설치 및 환경변수
- 설치
  - 설치 URL
    - https://www.confluent.io/ko-kr/previous-versions 접속 후 설치하고자 하는 버전 선택.
  - 설치
    - wget https://packages.confluent.io/archive/7.1/confluent-community-7.1.2.tar.gz?_ga=2.263210127.226026469.1712993899-2038344508.1712993899&_gl=1*p5wvuk*_ga*MjAzODM0NDUwOC4xNzEyOTkzODk5*_ga_D2D3EGKSGD*MTcxMjk5Mzg5OC4xLjEuMTcxMjk5MzkxOS4zOS4wLjA
  - 설치파일 이름 변경
    - mv 'confluent-community-7.1.2.tar.gz?_ga=2.263210127.226026469.1712993899-2038344508.1712993899' confluent-commu.tar.gz
  - 압축 해제
    - tar -xvf confluent-commu.tar.gz
- 환경변수
  - .bashrc 수정
    -  export CONFLUENT_HOME=/home/choi/confluent
    -  export PATH=.:$PATH:$CONFLUENT_HOME/bin
- Kakfa 설정 변경
  - {Confluent Home}/etc/kafka/server.properties
    - log.dirs=/home/choi/data/kafka-logs
  - {Confluent Home}/etc/kafka/zookeeper.properties
    - dataDir=/home/choi/data/zookeeper

### Kafka 명령어
- Zookeeper 실행
  - zookeeper-server-start $CONFLUENT_HOME/etc/kafka/zookeeper.properties
- Kafka 실행
  - kafka-server-start $CONFLUENT_HOME/etc/kafka/server.properties
- 토픽
  - 생성 (기본)
    - kafka-topics --bootstrap-server localhost:9092 --create --topic welcome-topic
  - 생성 (파티션 3개짜리 토픽 생성)
    - kafka-topics --bootstrap-server localhost:9092 --create --topic test_topic_02 --partitions 3
  - 목록
    - kafka-topics --bootstrap-server localhost:9092 --list
  - 상세
    - kafka-topics --bootstrap-server localhost:9092 --describe --topic test_topic_02
  - 삭제
    - kafka-topics --bootstrap-server localhost:9092 --delete --topic test_topic_02
- 메시지
  - 전송
    - kafka-console-producer --bootstrap-server localhost:9092 --topic test-topic
  - 조회
    - kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic
    - kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic --from-beginning (처음 접속 시, 오래된 offset부터 가져오기)
  - Key를 가지는 메시지 전송
    - kafka-console-producer --bootstrap-server localhost:9092 --topic test-topic --property key.separator=: --property parse.key=true
  - Key를 가지는 메시지 조회
    - kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic --property print.key=true --property print.value=true --from-beginning
  - Partition 번호와 함께 조회
    - kafka-console-consumer --bootstrap-server localhost:9092 --topic multipart-topic --from-beginning --property print.partition=true
- Consumer Group
  - Consumer Group ID가 'group_01'을 가지는 consumer를 생성
    - kafka-console-consumer --bootstrap-server localhost:9092 --group group_01 --topic multipart-topic --property print.key=true --property print.value=true --property print.partition=true
  - Consumer Group 목록
    - kafka-consumer-groups --bootstrap-server localhost:9092 --list
  - Consumer Group 상세
    - kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group 그룹명
    - 위의 명령어를 사용하여 LAG 정보를 통해 밀려있는 메시지를 확인할 수 있다.
  - Consumer Group 삭제
    - Group 내 Consumer들이 없을 경우, 일정 시간이 지나면 자동으로 삭제되지만 아래 명령어를 통해 즉시 삭제할 수도 있다.
    - kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group group_01
