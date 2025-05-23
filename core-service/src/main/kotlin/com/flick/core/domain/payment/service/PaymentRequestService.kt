package com.flick.core.domain.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.flick.common.error.CustomException
import com.flick.core.domain.payment.dto.*
import com.flick.core.infra.security.SecurityHolder
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.order.entity.OrderEntity
import com.flick.domain.order.enums.OrderStatus
import com.flick.domain.order.error.OrderError
import com.flick.domain.order.entity.PaymentEntity
import com.flick.domain.order.entity.PaymentRequestEntity
import com.flick.domain.order.enums.PaymentMethod
import com.flick.domain.order.enums.PaymentStatus
import com.flick.domain.order.error.PaymentRequestError
import com.flick.domain.order.repository.OrderRepository
import com.flick.domain.order.repository.PaymentRepository
import com.flick.domain.order.repository.PaymentRequestRepository
import com.flick.domain.transaction.entity.TransactionEntity
import com.flick.domain.transaction.enums.TransactionType
import com.flick.domain.transaction.repository.TransactionRepository
import com.flick.domain.user.entity.UserEntity
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.LocalDateTime

@Service
class PaymentRequestService(
    private val paymentRequestRepository: PaymentRequestRepository,
    private val paymentRepository: PaymentRepository,
    private val securityHolder: SecurityHolder,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val boothRepository: BoothRepository,
    private val objectMapper: ObjectMapper,
    private val transactionalOperator: TransactionalOperator
) {
    suspend fun getPaymentRequest(token: String): PaymentRequestResponse {
        val paymentRequest = paymentRequestRepository.findByToken(token)
            ?: throw CustomException(PaymentRequestError.PAYMENT_REQUEST_NOT_FOUND)

        return PaymentRequestResponse(
            id = paymentRequest.id!!,
            orderId = paymentRequest.orderId,
            method = paymentRequest.method,
        )
    }

    suspend fun confirmPaymentRequest(request: ConfirmPaymentRequestRequest) {
        var retryCount = 0
        val maxRetries = 3

        while (true) {
            try {
                val userId = securityHolder.getUserId()
                val paymentRequest = paymentRequestRepository.findByToken(request.token)
                    ?: throw CustomException(PaymentRequestError.PAYMENT_REQUEST_NOT_FOUND)

                validatePaymentRequestStatus(paymentRequest)

                transactionalOperator.executeAndAwait {
                    val order = orderRepository.findById(paymentRequest.orderId)
                        ?: throw CustomException(OrderError.ORDER_NOT_FOUND)

                    if (order.status != OrderStatus.PENDING)
                        throw CustomException(OrderError.ORDER_NOT_PENDING)

                    val user = userRepository.findById(userId)
                        ?: throw CustomException(UserError.USER_NOT_FOUND)

                    if (user.balance < order.totalAmount)
                        throw CustomException(UserError.INSUFFICIENT_BALANCE)

                    processPayment(user, order, paymentRequest)
                }
                break
            } catch (e: OptimisticLockingFailureException) {
                if (++retryCount >= maxRetries) throw e
            }
        }
    }

    private fun validatePaymentRequestStatus(request: PaymentRequestEntity) {
        when (request.status) {
            PaymentStatus.COMPLETED -> throw CustomException(PaymentRequestError.PAYMENT_REQUEST_ALREADY_COMPLETED)
            PaymentStatus.FAILED -> throw CustomException(PaymentRequestError.PAYMENT_REQUEST_FAILED)
            PaymentStatus.EXPIRED -> throw CustomException(PaymentRequestError.PAYMENT_REQUEST_EXPIRED)
            else -> {}
        }
    }

    private suspend fun processPayment(user: UserEntity, order: OrderEntity, paymentRequest: PaymentRequestEntity) {
        val userId = user.id!!
        val now = LocalDateTime.now()
        val newBalance = user.balance - order.totalAmount

        userRepository.save(user.copy(balance = newBalance))

        val transaction = transactionRepository.save(
            TransactionEntity(
                userId = userId,
                type = TransactionType.PAYMENT,
                amount = order.totalAmount,
                balanceAfter = newBalance,
                orderId = order.id,
                memo = "주문 #${order.boothOrderNumber} 결제",
                createdAt = now
            )
        )

        paymentRepository.save(
            PaymentEntity(
                orderId = paymentRequest.orderId,
                requestId = paymentRequest.id!!,
                userId = userId,
                amount = order.totalAmount,
                transactionId = transaction.id!!,
                createdAt = now
            )
        )

        paymentRequestRepository.save(
            paymentRequest.copy(
                status = PaymentStatus.COMPLETED,
                userId = userId,
                completedAt = now,
                updatedAt = now
            )
        )

        val updatedOrder = orderRepository.save(
            order.copy(
                status = OrderStatus.PAID,
                paidAt = now,
                updatedAt = now
            )
        )

        val booth = boothRepository.findById(updatedOrder.boothId)
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

        sendBoothSalesUpdatedEvent(booth.id!!, booth.totalSales)
        sendPaymentStatusUpdate(
            paymentRequest.id!!,
            order.id!!,
            PaymentStatus.COMPLETED,
            paymentRequest.method,
            order.totalAmount
        )
        sendOrderCompletedNotification(userId, order.id!!, booth.name, order.totalAmount)
    }

    private fun sendBoothSalesUpdatedEvent(boothId: Long, totalSales: Long) {
        val event = BoothSalesUpdatedEvent(
            boothId = boothId,
            totalSales = totalSales
        )
        kafkaTemplate.send("booth-sales-updated", objectMapper.writeValueAsString(event))
    }

    private fun sendPaymentStatusUpdate(
        requestId: Long,
        orderId: Long,
        status: PaymentStatus,
        paymentMethod: PaymentMethod?,
        amount: Long?
    ) {
        val event = PaymentStatusUpdateEvent(
            requestId = requestId,
            orderId = orderId,
            status = status,
            paymentMethod = paymentMethod,
            amount = amount
        )
        kafkaTemplate.send("payment-status-update", objectMapper.writeValueAsString(event))
    }

    private fun sendOrderCompletedNotification(userId: Long, orderId: Long, boothName: String, amount: Long) {
        val event = OrderCompletedEvent(
            userId = userId,
            orderId = orderId,
            boothName = boothName,
            totalAmount = amount
        )
        kafkaTemplate.send("order-completed", objectMapper.writeValueAsString(event))
    }
}