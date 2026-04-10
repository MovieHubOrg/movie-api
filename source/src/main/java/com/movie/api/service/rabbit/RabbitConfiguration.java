package com.movie.api.service.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    @Value("${rabbitmq.update.video.queue}")
    private String updateVideoQueue;

    @Value("${rabbitmq.account.exchange}")
    private String accountExchange;

    @Value("${rabbitmq.account.queue.movie}")
    private String accountMovieQueue;

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Queue updateVideoQueue() {
        return new Queue(updateVideoQueue, true);
    }

    @Bean
    public FanoutExchange accountFanoutExchange() {
        return new FanoutExchange(accountExchange, true, false);
    }

    @Bean
    public Queue accountMovieQueue() {
        return QueueBuilder.durable(accountMovieQueue).build();
    }

    @Bean
    public Binding accountMovieQueueBinding() {
        return BindingBuilder
                .bind(accountMovieQueue())
                .to(accountFanoutExchange());
    }
}
