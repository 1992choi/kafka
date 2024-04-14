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
### 메시지
- 메시지는 Producer를 통해 전송 시 파티셔너를 통해 토픽의 어떤 파티션으로 전송되어야 할 지 미리 결정된다.
- Topic이 여러 개의 파티션을 가질 때, 메시지의 전송 순서가 보장되지 않은 채로 Consumer에서 읽혀질 수 있다. (파티션 내에서의 순서는 보장된다.)
- Key값을 가지지 않는 경우, 라운드 로빈, 스티키 파티션 등의 파티션 전략 등이 선택되어 파티션 별로 메시지가 전송될 수 있다.
- Key값을 가지는 경우, 특정 파티션으로 고정되어 전송된다.

<br><hr><br>

## 환경 구축
### 우분투 설치
- https://velog.io/@suzu11/Mac-M1-%EC%97%90%EC%84%9C-%EC%9A%B0%EB%B6%84%ED%88%AC-%EC%84%A4%EC%B9%98%ED%95%98%EA%B8%B0-UTM
### 우분투 고정 IP 할당
- 
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
   
