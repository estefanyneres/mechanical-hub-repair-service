package com.fiap.mechanical_hub.repair_service.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mesma exchange do Saga que o core-service declara (nome/tipo/durabilidade
 * tem que ser idênticos entre os dois -- o RabbitMQ rejeita uma segunda
 * declaração da "mesma" exchange com propriedades diferentes). Fila própria
 * deste serviço, igual ao padrão já usado no core-service.
 *
 * Contrato (ver RabbitMQConfig do core-service pro contrato completo):
 *   repair-service CONSOME (fila repair-service.eventos.queue):
 *     - "pagamento.confirmado"  {serviceOrderId}  (billing-service -- primeira tentativa de reserva)
 *     - "estoque.reabastecido"  {serviceOrderId}  (stock-service, ainda não implementado -- nova
 *                                                   tentativa de reserva quando existir o publicador)
 *   repair-service PUBLICA:
 *     - "material.reservado"    {serviceOrderId}           (reserva OK, em qualquer dos dois gatilhos)
 *     - "material.indisponivel" {serviceOrderId, motivo}   (faltou peça)
 *     - "execucao.iniciada"     {serviceOrderId}           (TASK-048 - mecanico marcou a PRIMEIRA tarefa da
 *                                                           OS como INICIADO. Core-service reage movendo a
 *                                                           OS pra EM_EXECUCAO -- antes disso nao acontecia,
 *                                                           entao a OS nunca saia de PRONTO_PARA_EXECUCAO e
 *                                                           o execucao.finalizada subsequente caia num
 *                                                           InvalidOrderTransitionException, TASK-046)
 *     - "execucao.finalizada"   {serviceOrderId}           (TASK-023/024 - todas as tarefas da OS concluídas)
 *     - "execucao.falhou"       {serviceOrderId, motivo}  (TASK-024 - falha durante a execucao manual ja
 *                                                           iniciada, reportada pelo mecanico via
 *                                                           POST /service-orders/{id}/execution-failure;
 *                                                           NAO e sobre falta de material, isso ja e
 *                                                           "material.indisponivel". O core-service reage
 *                                                           voltando a OS pra EM_DIAGNOSTICO)
 */
@Configuration
public class RabbitMQConfig {

    public static final String SAGA_EXCHANGE = "saga.exchange";
    public static final String REPAIR_SERVICE_QUEUE = "repair-service.eventos.queue";

    public static final String ROUTING_KEY_PAGAMENTO_CONFIRMADO = "pagamento.confirmado";
    public static final String ROUTING_KEY_ESTOQUE_REABASTECIDO = "estoque.reabastecido";
    public static final String ROUTING_KEY_MATERIAL_RESERVADO = "material.reservado";
    public static final String ROUTING_KEY_MATERIAL_INDISPONIVEL = "material.indisponivel";
    public static final String ROUTING_KEY_EXECUCAO_INICIADA = "execucao.iniciada";
    public static final String ROUTING_KEY_EXECUCAO_FINALIZADA = "execucao.finalizada";
    public static final String ROUTING_KEY_EXECUCAO_FALHOU = "execucao.falhou";

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE, true, false);
    }

    @Bean
    public Queue repairServiceQueue() {
        return new Queue(REPAIR_SERVICE_QUEUE, true);
    }

    @Bean
    public Binding pagamentoConfirmadoBinding(Queue repairServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(repairServiceQueue).to(sagaExchange).with(ROUTING_KEY_PAGAMENTO_CONFIRMADO);
    }

    @Bean
    public Binding estoqueReabastecidoBinding(Queue repairServiceQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(repairServiceQueue).to(sagaExchange).with(ROUTING_KEY_ESTOQUE_REABASTECIDO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    // Sem Jackson2JsonMessageConverter aqui: ele tenta resolver o header
    // __TypeId__ (classe de quem publicou) via Class.forName(), que nunca
    // existe no classpath de quem consome -- descartava a mensagem calada.
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }
}
