# Kafka

<br><br>

## 정리
### Topic
- ![image](https://github.com/Young-Geun/Kafka/assets/27760576/e5d88001-3adf-4b47-954a-eae9f5e125da)
- Topic은 파티션으로 구성된 일련의 로그 파일이다.
- RDBMS의 테이블과 유사항 기능이다.
- Key와 Value 기반의 메시지 구조이며, Value는 어떠한 타입도 가능하다.
- 시간의 흐름에 따라 메시지가 순차적으로 물리적인 파일에 써진다.
- 파티션은 Kafka의 병렬 성능과 가용성 기능의 핵심 요소이다.

<br><br>

## 설치 및 실행 명령어
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

### Kafka 명령어
- Zookeeper 실행
  - zookeeper-server-start $CONFLUENT_HOME/etc/kafka/zookeeper.properties
- Kafka 실행
  - kafka-server-start $CONFLUENT_HOME/etc/kafka/server.properties
- 토픽 생성
  - kafka-topics --bootstrap-server localhost:9092 --create --topic welcome-topic
