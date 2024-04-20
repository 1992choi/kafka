package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class SimpleConsumer {

    public static final Logger logger = LoggerFactory.getLogger(SimpleConsumer.class.getName());

    public static void main(String[] args) {
        String topicName = "simple-topic";

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.64.2:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "group-01");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(props);
        kafkaConsumer.subscribe(List.of(topicName));

        while (true) {
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord record : consumerRecords) {
                logger.info("record key:{}, record value:{}, partition:{}",
                        record.key(), record.value(), record.partition());
            }
        }

        // 원래는 close()를 반드시 수행해야하지만, 현재 테스트 코드상 while문에서 빠져나오는 케이스가 없으므로 임시로 주석처리.
        // 만약 close 처리를 해주지 않으면, rebalancing을 바로 해주지 못하고, Heart Beat를 받지못하여 종료되는 것으로 처리된 후 rebalancing을 진행한다. (좋은 방법은 close를 명시적으로 선언하여 rebalancing 되도록 처리하는 것)
        // kafkaConsumer.close();
    }
}