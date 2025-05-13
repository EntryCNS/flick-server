package com.flick.notification.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.flick.common.utils.logger
import com.flick.domain.notification.enums.NotificationType
import com.flick.notification.dto.NoticeCreatedEvent
import com.flick.notification.dto.OrderCompletedEvent
import com.flick.notification.dto.PaymentRequestEvent
import com.flick.notification.dto.PointChargedEvent
import com.flick.notification.service.NotificationService
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationKafkaListener(
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper
) {
    private val log = logger()

    @KafkaListener(topics = ["payment-request"], groupId = "notification-group")
    fun handlePaymentRequest(message: String) {
        log.info("Received payment request: $message")

        try {
            val event = objectMapper.readValue<PaymentRequestEvent>(message)

            runBlocking {
                notificationService.createNotificationAndSend(
                    userId = event.userId,
                    type = NotificationType.PAYMENT_REQUEST,
                    title = "결제 요청",
                    body = "${event.boothName}에서 ${event.totalAmount}원 결제 요청이 있습니다.",
                    data = objectMapper.writeValueAsString(
                        mapOf(
                            "orderId" to event.orderId,
                            "token" to event.token
                        )
                    )
                )
            }
        } catch (e: Exception) {
            log.error("Failed to process payment-request: $message", e)
        }
    }

    @KafkaListener(topics = ["order-completed"], groupId = "notification-group")
    fun handleOrderCompleted(message: String) {
        log.info("Received order completed event: $message")

        try {
            val event = objectMapper.readValue<OrderCompletedEvent>(message)

            runBlocking {
                notificationService.createNotificationAndSend(
                    userId = event.userId,
                    type = NotificationType.ORDER_COMPLETED,
                    title = "주문 완료",
                    body = "${event.boothName}에서 ${event.totalAmount}원 결제가 완료되었습니다.",
                    data = objectMapper.writeValueAsString(mapOf("orderId" to event.orderId))
                )
            }
        } catch (e: Exception) {
            log.error("Failed to process order-completed: $message", e)
        }
    }

    @KafkaListener(topics = ["point-charged"], groupId = "notification-group")
    fun handlePointCharged(message: String) {
        log.info("Received point charged event: $message")

        try {
            val event = objectMapper.readValue<PointChargedEvent>(message)

            runBlocking {
                notificationService.createNotificationAndSend(
                    userId = event.userId,
                    type = NotificationType.POINT_CHARGED,
                    title = "포인트 충전",
                    body = "${event.amount}원이 충전되었습니다. 현재 잔액: ${event.balanceAfter}원",
                    data = objectMapper.writeValueAsString(
                        mapOf(
                            "amount" to event.amount,
                            "balanceAfter" to event.balanceAfter
                        )
                    )
                )
            }
        } catch (e: Exception) {
            log.error("Failed to process point-charged: $message", e)
        }
    }

    @KafkaListener(topics = ["notice-created"], groupId = "notification-group")
    fun handleNoticeCreated(message: String) {
        log.info("Received notice created event: $message")

        try {
            val event = objectMapper.readValue<NoticeCreatedEvent>(message)

            runBlocking {
                notificationService.createNotificationAndSend(
                    userId = event.userId,
                    type = NotificationType.NOTICE_CREATED,
                    title = "새 공지사항",
                    body = event.title,
                    data = objectMapper.writeValueAsString(
                        mapOf(
                            "id" to event.id
                        )
                    )
                )
            }
        } catch (e: Exception) {
            log.error("Failed to process notice-created: $message", e)
        }
    }
}