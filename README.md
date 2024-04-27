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
### Producer와 재전송
- max.block.ms
  - Send( ) 호출 시 Record Accumulator에 입력하지 못하고 block되는 최대 시간. 초과 시 Timeout Exception 발생.
- linger.ms
  - Sender Thread가 Record Accumulator에서 배치별로 가져가기 위한 최대 대기시간
- request.timeout.ms
  - 전송에 걸리는 최대 시간. 전송 재 시도 대기시간 제외.
  - 초과시 retry를 하거나 Timout Exception 발생
- retry.backoff.ms
  - 전송 재 시도를 위한 대기 시간
- deliver.timeout.ms
  - Producer 메시지(배치) 전송에 허용된 최대 시간. 초과시 Timeout Exception 발생.
### Producer와 멱등성(Idempotent)
- 멱등성 프로듀서는 동일한 데이터를 여러번 전송하더라도 카프카 클러스터에 단 한번만 저장됨을 의미한다.
- 프로듀서는 브로커로부터 ACK를 받은 다음에 다음 메시지 전송하되, Producer ID와 메시지 Sequence를 Header에 저장하여 전송한다.
- 메시지 Sequence는 메시지의 고유번호로써 0부터 시작하여 순차적으로 증가하고, Producer ID는 Producer가 기동시마다 새롭게 생성된다.
- 브로커에서 메시지 Sequece가 중복 될 경우, 이를 메시지 로그에 기록하지 않고 Ack만 전송한다.
- 브로커는 Producer가 보낸 메시지의 Sequence가 브로커가 가지고 있는 메시지의 Sequence보다 1만큼 큰 경우에만 브로커에 저장한다.
- Idempotence를 위한 Producer 설정
  - enable.idempotence = true
  - acks = all
  - retries는 0 보다 큰 값
  - max.in.flight.requests.per.connection은 1에서 5사이 값
- 설정 시 유의사항
  - Kafka 3.0 버전부터는 Producer의 기본 설정이 Idempotence이다.
  - enable.idempotence의 값은 기본값이 true이다.
  - enable.idempotence=true를 명시적으로 서술하지 않고, 다른 파라미터를 잘못 설정하면 Producer는 정상적으로 메시지를 보내지만 idempotence로는 동작하지 않는다.
  - enable.idempotence=true를 명시적으로 서술하고, 다른 파라미터를 잘못 설정하면 Config 오류가 발생하며 Producer가 기동되지 않는다.
### Consumer와 subscribe, poll, commit
- ![image](https://github.com/Young-Geun/Kafka/assets/27760576/6d794505-2bf7-45a4-8f0d-4cefffc84002)
- subscribe
  - Consumer는 subscribe()를 호출하여 읽어 들이려는 토픽을 등록한다.
- poll
  - Consumer는 poll( ) 메소드를 이용하여 주기적으로 브로커의 토픽 파티션에서 메시지를 가져온다. 
- commit
  - 메시지를 성공적으로 가져 왔으면 commit을 통해서 __consumer_offse에 다음에 읽을 offset 위치를 기재한다.
### Consumer 구성요소와 poll() 동작원리
- 구성요소
  - ![image](https://github.com/Young-Geun/Kafka/assets/27760576/b502dcdb-55f7-4d38-9907-256e3b5f14fd)
  - Fetcher & ConsumerNetworkClient
    - 2개의 컴포넌트를 통해, 파티션의 데이터를 해당 컨슈머 클라이언트로 가져온다.
  - SubscriptionState
    - 파티션의 구독 상태를 관리한다.
  - ConsumerCoordinator
    - 해당 Consumer Group의 리더 컨슈머가 누구인지, 해당 컨슈머의 옵션 등을 관리한다.
  - HeartBeat Thread
    - 하트비트 체크를 위한 별도의 스레드.
- poll() 동작원리
  - ![image](https://github.com/Young-Geun/Kafka/assets/27760576/c4f9b68f-10cd-4b35-8996-d72aec142737)
  - ConsumerNetworkClient는 비동기로 계속 브로커의 메시지를 가져와서 Linked Queue에 저장한다.
  - Linked Queue에 데이터가 있을 경우, Fetcher는 데이터를 가져오고 반환하며 poll() 수행을 완료한다.
  - Linked Queue에 데이터가 없을 경우, 1000ms까지 Broker에 메시지 요청 후 poll() 수행을 완료한다.
### Group Coordinator와 Consumer Group
- Group Coordinator와 Consumer Group 관계
  - Consumer Group내에 Consumer가 변경(추가, 종료 등)되면 Group Coordinator는 Consumer Group내의 Consumer들에게 파티션을 재할당하는 Rebalancing을 수행하도록 지시한다.
- Rebalancing 절차(기존 Consumer 종료 예시)
  - ![image](https://github.com/Young-Geun/Kafka/assets/27760576/3b3ed4df-6f63-433a-880b-f5d85fe271de)
### Consumer 정적 그룹 멤버십 (Static Group MemberShip)
- 필요성
  - 많은 Consumer를 가지는 Consumer Group에서 Rebalance가 발생하면, 모든 Consumer들이 Rebalance를 수행하므로 많은 시간이 소모되고 대량 데이터 처리 시 Lag가 더 길어질 수 있다.
- Static Group MemberShip
  - ![image](https://github.com/Young-Geun/Kafka/assets/27760576/7bb62806-ab43-4abe-b939-e689be6b3ae6)
  - 그룹 인스턴스 ID를 컨슈머 구성 값의 일부로 특정하게 되면 컨슈머는 정적 멤버가 된다.
  - 정적 멤버가 된 컨슈머 3번이 그룹에서 나갈 경우에도 파티션 2번이 다른 컨슈머로 재할당 되지 않는다.
  - 이 때 밀리초에 해당하는 세션 시간 내에 다시 합류하게 되면 파티션 2번이 Consumer 3번에게 다시 할당이 된다.
  - 만약 시간 안에 못 들어오게되면 Rebalance가 발생하고 2번 파티션이 다른 컨슈머로 이동된다.
### Consumer Rebalancing 방식
- Eager Rebalance
  - Rebalancing이 실행되는 중에 모든 컨슈머가 읽기 작업을 멈추고 자신에게 할당된 모든 파티션에 대한 소유권을 포기한 뒤, Rejoin하는 방식이다.
  - 모든 Consumer가 잠시 메시지를 읽지 않는 시간으로 인해 Lag가 상대적으로 크게 발생할 가능성 있다.
  - 파티션 할당 전략(partition.assignment.stragegy)중 Range, Round Robin, Sticky 방식이 여기에 해당한다.
- Incremental Cooperative Rebalance
  - Rebalance 수행 시 기존 Consumer들의 모든 파티션 할당을 취소하지 않고, 대상이 되는 Consumer들에 대해서 파티션에 따라 점진적으로 Consumer를 할당하면서 Rebalance를 수행하는 방식이다.
  - 파티션 할당 전략(partition.assignment.stragegy)중 Cooperative Sticky에 해당한다.
### Consumer 파티션 할당 전략
- https://baebalja.tistory.com/629
### Consumer와 Auto Commit
- https://junuuu.tistory.com/886
### Consumer와 Manual Commit
- https://mycup.tistory.com/437
### Kafka Replication
- https://colevelup.tistory.com/19

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
  - 생성 (다중 복제 토픽)
    - kafka-topics --bootstrap-server localhost:9092 --create --topic topic-p3r3 --partitions 3 --replication-factor 3
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

### PostgreSQL
- 설치
  - sudo apt install postgresql postgresql-client
- 종료
  - sudo systemctl stop postgresql
- 실행
  - sudo systemctl start postgresql
- Postgres 계정으로 전환
  - sudo su - postgres
- 데이터베이스 접속
  - psql
