package com.flick.notification.listener

import com.flick.domain.notification.enums.NotificationType
import com.flick.notification.dto.OrderCompletedEvent
import com.flick.notification.dto.PaymentRequestEvent
import com.flick.notification.dto.PointChargedEvent
import com.flick.notification.service.NotificationService
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component


@Component
class NotificationKafkaListener(private val notificationService: NotificationService) {
    @KafkaListener(topics = ["payment-request"], groupId = "notification-group")
    fun handlePaymentRequest(event: PaymentRequestEvent) {
        runBlocking {
            notificationService.createNotificationAndSend(
                userId = event.userId,
                type = NotificationType.PAYMENT_REQUEST,
                title = "결제 요청",
                content = "${event.boothName}에서 ${event.totalAmount}원 결제 요청이 있습니다.",
                data = """{"orderId": "${event.orderId}", "requestCode": "${event.requestCode}"}"""
            )
        }
    }

    @KafkaListener(topics = ["order-completed"], groupId = "notification-group")
    fun handleOrderCompleted(event: OrderCompletedEvent) {
        runBlocking {
            notificationService.createNotificationAndSend(
                userId = event.userId,
                type = NotificationType.ORDER_COMPLETED,
                title = "주문 완료",
                content = "${event.boothName}에서 ${event.totalAmount}원 결제가 완료되었습니다.",
                data = """{"orderId": "${event.orderId}"}"""
            )
        }
    }

    @KafkaListener(topics = ["point-charged"], groupId = "notification-group")
    fun handlePointCharged(event: PointChargedEvent) {
        runBlocking {
            notificationService.createNotificationAndSend(
                userId = event.userId,
                type = NotificationType.POINT_CHARGED,
                title = "포인트 충전",
                content = "${event.amount}원이 충전되었습니다. 현재 잔액: ${event.balanceAfter}원",
                data = """{"amount": "${event.amount}", "balanceAfter": "${event.balanceAfter}"}"""
            )
        }
    }
}