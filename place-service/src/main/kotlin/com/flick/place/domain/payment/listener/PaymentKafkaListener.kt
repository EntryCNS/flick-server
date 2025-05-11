package com.flick.place.domain.payment.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.flick.common.utils.logger
import com.flick.domain.order.enums.OrderStatus
import com.flick.domain.user.enums.PaymentStatus
import com.flick.domain.payment.repository.OrderRepository
import com.flick.domain.payment.repository.PaymentRequestRepository
import com.flick.place.domain.payment.dto.PaymentRequestEventDto
import com.flick.place.domain.payment.dto.PaymentStatusUpdateDto
import com.flick.place.infra.websocket.PaymentWebSocketHandler
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PaymentKafkaListener(
    private val objectMapper: ObjectMapper,
    private val orderRepository: OrderRepository,
    private val paymentWebSocketHandler: PaymentWebSocketHandler,
    private val paymentRequestRepository: PaymentRequestRepository,
) {
    private val log = logger()

    @KafkaListener(topics = ["payment-request"], groupId = "place-service")
    fun handlePaymentRequest(message: String) {
        try {
            val event = objectMapper.readValue<PaymentRequestEventDto>(message)
            log.info("Received payment request: orderId=${event.orderId}, userId=${event.userId}")
        } catch (e: Exception) {
            log.error("Failed to process payment-request: $message", e)
        }
    }

    @KafkaListener(topics = ["payment-status-update"], groupId = "place-service")
    fun handlePaymentStatusUpdate(message: String) {
        try {
            val update = objectMapper.readValue<PaymentStatusUpdateDto>(message)
            log.info("Received payment status update: requestId=${update.requestId}, status=${update.status}")

            runBlocking {
                val paymentRequest = paymentRequestRepository.findById(update.requestId)

                if (paymentRequest != null) {
                    val websocketMessage = objectMapper.writeValueAsString(update)
                    paymentWebSocketHandler.sendPaymentUpdateMessage(update.requestId, websocketMessage)

                    if (update.status == PaymentStatus.COMPLETED) {
                        val order = orderRepository.findById(paymentRequest.orderId)
                        order?.let {
                            orderRepository.save(it.copy(
                                status = OrderStatus.PAID,
                                paidAt = update.processedAt
                            ))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            log.error("Failed to process payment-status-update: $message", e)
        }
    }

    @KafkaListener(topics = ["payment-failed"], groupId = "place-service")
    fun handlePaymentFailed(message: String) {
        try {
            val update = objectMapper.readValue<PaymentStatusUpdateDto>(message)
            log.info("Received payment failed: requestId=${update.requestId}")

            runBlocking {
                val paymentRequest = paymentRequestRepository.findById(update.requestId)

                if (paymentRequest != null) {
                    val websocketMessage = objectMapper.writeValueAsString(update)
                    paymentWebSocketHandler.sendPaymentUpdateMessage(update.requestId, websocketMessage)
                }
            }
        } catch (e: Exception) {
            log.error("Failed to process payment-failed: $message", e)
        }
    }

    @KafkaListener(topics = ["order-expired"], groupId = "place-service")
    fun handleOrderExpired(message: String) {
        try {
            val orderId = message.toLong()
            log.info("Received order expired: orderId=$orderId")

            runBlocking {
                val order = orderRepository.findById(orderId)
                order?.let {
                    orderRepository.save(it.copy(
                        status = OrderStatus.EXPIRED
                    ))

                    val paymentRequests = paymentRequestRepository.findAllByOrderId(orderId)
                    paymentRequests.collect { request ->
                        val update = PaymentStatusUpdateDto(
                            requestId = request.id!!,
                            orderId = orderId,
                            status = PaymentStatus.EXPIRED
                        )
                        val websocketMessage = objectMapper.writeValueAsString(update)
                        paymentWebSocketHandler.sendPaymentUpdateMessage(request.id!!, websocketMessage)
                    }
                }
            }
        } catch (e: Exception) {
            log.error("Failed to process order-expired: $message", e)
        }
    }
}