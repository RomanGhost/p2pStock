package com.example.p2p_stock.configs

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.support.RetryTemplate

@EnableKafka
@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupID: String
) {

    // Producer Configuration
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.BATCH_SIZE_CONFIG to 16384,
            ProducerConfig.LINGER_MS_CONFIG to 1
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }

    // Consumer Configuration
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val configProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupID,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
            ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to "300000"
        )
        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory()
        factory.setConcurrency(3)  // Количество параллельных потребителей
        return factory
    }

    // Error Handler
    @Bean
    fun errorHandler(): DefaultErrorHandler {
        val errorHandler = DefaultErrorHandler()
        return errorHandler
    }

    @Bean
    fun retryTemplate(): RetryTemplate {
        val retryTemplate = RetryTemplate()
        val backOffPolicy = ExponentialBackOffPolicy()
        backOffPolicy.initialInterval = 1000L
        backOffPolicy.multiplier = 2.0
        backOffPolicy.maxInterval = 10000L
        retryTemplate.setBackOffPolicy(backOffPolicy)
        return retryTemplate
    }

    @Bean
    fun deadLetterPublishingRecoverer(): DeadLetterPublishingRecoverer {
        return DeadLetterPublishingRecoverer(kafkaTemplate())
    }
}
