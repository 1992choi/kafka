package com.example.kafka;

import com.github.javafaker.Faker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class PizzaProducer {

    public static final Logger logger = LoggerFactory.getLogger(PizzaProducer.class.getName());

    public static void sendPizzaMessage(KafkaProducer<String, String> kafkaProducer,
                                        String topicName, int iterCount,
                                        int interIntervalMillis, int intervalMillis,
                                        int intervalCount, boolean sync) {
        PizzaMessage pizzaMessage = new PizzaMessage();
        int iterSeq = 0;
        long seed = 2024;
        Random random = new Random(seed);
        Faker faker = Faker.instance(random);

        long startTime = System.currentTimeMillis();

        while (iterSeq++ != iterCount) {
            HashMap<String, String> pMessage = pizzaMessage.produce_msg(faker, random, iterSeq);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName,
                    pMessage.get("key"), pMessage.get("message"));
            sendMessage(kafkaProducer, producerRecord, pMessage, sync);

            if ((intervalCount > 0) && (iterSeq % intervalCount == 0)) {
                try {
                    logger.info("####### IntervalCount:" + intervalCount +
                            " intervalMillis:" + intervalMillis + " #########");
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }

            if (interIntervalMillis > 0) {
                try {
                    logger.info("interIntervalMillis:" + interIntervalMillis);
                    Thread.sleep(interIntervalMillis);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }

        }
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;

        logger.info("{} millisecond elapsed for {} iterations", timeElapsed, iterCount);
    }

    public static void sendMessage(KafkaProducer<String, String> kafkaProducer,
                                   ProducerRecord<String, String> producerRecord,
                                   HashMap<String, String> pMessage, boolean sync) {
        if (!sync) {
            kafkaProducer.send(producerRecord, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("async message:" + pMessage.get("key") + " partition:" + metadata.partition() +
                            " offset:" + metadata.offset());
                } else {
                    logger.error("exception error from broker " + exception.getMessage());
                }
            });
        } else {
            try {
                RecordMetadata metadata = kafkaProducer.send(producerRecord).get();
                logger.info("sync message:" + pMessage.get("key") + " partition:" + metadata.partition() +
                        " offset:" + metadata.offset());
            } catch (ExecutionException e) {
                logger.error(e.getMessage());
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        String topicName = "pizza-topic-p3r3";

        Properties props = new Properties();
        /* Multi Brokers 접속 방법
            - 각 각의 브로커들을 모두 접속하는 것이 아니라, 하나의 브로커에 접속을 하는 형태이다.
              각 각의 브로커들은 동일한 메타 정보를 서로 공유하기 때문에 하나의 브로커에만 접속을 하더라도 구성을 알 수 있다.
              그럼에도 불구하고 브로커의 접속 정보를 모두 명시하는 이유는
              혹시 하나의 브로커가 다운되어서 접속을 못하는 경우를 대비하기 위함이다.
              Ex) 192.168.64.2:9092만 명시해도 되지만, 192.168.64.2:9092가 다운되었을 경우는 192.168.64.2:9092로 접속이 불가능하니
                  192.168.64.2:9093로 접속하여 메타 정보를 얻기 위함.
        */
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.64.2:9092, 192.168.64.2:9093, 192.168.64.2:9094");

        // props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.64.2:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // ACK 설정 테스트
        // props.setProperty(ProducerConfig.ACKS_CONFIG, "0");

        // BATCH 설정 테스트
        // props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "32000");
        //props.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");

        // 멱등성 설정 테스트 #1 - enable.idempotence를 명시적으로 선언하지 않고 잘못된 설정을 하면 기동은 되나 정상적으로 동작하지 않는다.
        // props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        //props.setProperty(ProducerConfig.ACKS_CONFIG, "0");

        // 멱등성 설정 테스트 #2 - enable.idempotence를 명시적으로 선언하고 잘못된 설정을 하면 기동되지 않는다.
        //props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        //props.setProperty(ProducerConfig.ACKS_CONFIG, "0");

        //KafkaProducer object creation
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(props);

        sendPizzaMessage(kafkaProducer, topicName, -1, 1000, 0, 0, false);

        kafkaProducer.close();
    }

}
