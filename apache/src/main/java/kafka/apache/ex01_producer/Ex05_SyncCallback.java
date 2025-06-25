package kafka.apache.ex01_producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/*
    레코드 전송 결과를 확인하는 프로듀서
    - 레코드 전송 후 결과를 받는다.
 */
public class Ex05_SyncCallback {
    private final static Logger logger = LoggerFactory.getLogger(Ex05_SyncCallback.class);
    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {

        Properties configs = new Properties();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        /*
            아래 옵션을 0으로 설정할 경우
            - 원래 기본값은 1이다.
            - 하지만 0으로 설정할 경우
              - ACKS가 0이면, 전송 성공 여부를 받지않는 다는 옵션이다. 때문에 metadata는 의미없는 값을 받는 것과 다름없다. (partition 정보는 알 수 있지만, offset 정보는 알 수 없기에 -1로 응답된다.)
              - 즉 설정값이 0이면 아래 로직은 의미없는 로직이 된다는 것이다.
         */
        // configs.put(ProducerConfig.ACKS_CONFIG, "0");

        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);

        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "Pangyo", "Pangyo");
        try {
            RecordMetadata metadata = producer.send(record).get();
            logger.info("partition = {}", metadata.partition()); // partition = 1
            logger.info("offset = {}", metadata.offset()); // offset = 3
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            producer.flush();
            producer.close();
        }
    }
}
