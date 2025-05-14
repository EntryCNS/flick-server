package com.flick.place.domain.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.flick.common.error.CustomException
import com.flick.common.utils.logger
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.order.entity.OrderEntity
import com.flick.domain.order.enums.OrderStatus
import com.flick.domain.order.error.OrderError
import com.flick.domain.order.entity.PaymentRequestEntity
import com.flick.domain.order.enums.PaymentMethod
import com.flick.domain.order.repository.OrderRepository
import com.flick.domain.order.repository.PaymentRequestRepository
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import com.flick.place.domain.payment.dto.*
import com.flick.place.infra.security.SecurityHolder
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class PaymentService(
    private val orderRepository: OrderRepository,
    private val paymentRequestRepository: PaymentRequestRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val boothRepository: BoothRepository,
    private val objectMapper: ObjectMapper,
    private val securityHolder: SecurityHolder,
    private val userRepository: UserRepository,
    private val transactionalOperator: TransactionalOperator
) {
    private val log = logger()

    suspend fun createQrPayment(request: CreateQrPaymentRequest): CreateQrPaymentResponse =
        transactionalOperator.executeAndAwait {
            val order = getPendingOrder(request.orderId)
            val token = generatePaymentToken()

            val paymentRequest = paymentRequestRepository.save(
                PaymentRequestEntity(
                    orderId = order.id!!,
                    method = PaymentMethod.QR_CODE,
                    token = token,
                )
            )

            CreateQrPaymentResponse(
                id = paymentRequest.id!!,
                token = token
            )
        }

    suspend fun createStudentIdPayment(request: CreateStudentIdPaymentRequest): CreateStudentIdPaymentResponse {
        val (response, event) = transactionalOperator.executeAndAwait {
            val order = getPendingOrder(request.orderId)
            val token = generatePaymentToken()

            val paymentRequest = paymentRequestRepository.save(
                PaymentRequestEntity(
                    orderId = order.id!!,
                    method = PaymentMethod.STUDENT_ID,
                    token = token,
                )
            )

            val booth = boothRepository.findById(order.boothId)
                ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

            val user = userRepository.findByGradeAndRoomAndNumber(
                grade = request.studentId.first().digitToInt(),
                room = request.studentId[1].digitToInt(),
                number = request.studentId.substring(2).toInt()
            ) ?: throw CustomException(UserError.USER_NOT_FOUND)

            val event = PaymentRequestEventDto(
                userId = user.id!!,
                orderId = order.id!!,
                boothName = booth.name,
                totalAmount = order.totalAmount,
                token = token
            )

            CreateStudentIdPaymentResponse(id = paymentRequest.id!!) to event
        }

        sendPaymentRequestNotification(event)
        return response
    }

    private suspend fun getPendingOrder(orderId: Long): OrderEntity {
        val order = orderRepository.findByIdAndBoothId(orderId, securityHolder.getBoothId())
            ?: throw CustomException(OrderError.ORDER_NOT_FOUND)

        if (order.status != OrderStatus.PENDING) {
            throw CustomException(OrderError.ORDER_NOT_PENDING)
        }

        return order
    }

    private fun generatePaymentToken() = (1..32).map { (('a'..'z') + ('1'..'9')).random() }.joinToString("")

    private suspend fun sendPaymentRequestNotification(event: PaymentRequestEventDto) {
        try {
            val eventJson = objectMapper.writeValueAsString(event)

            kafkaTemplate.send("payment-request", eventJson)
            log.info("Payment request notification sent for order ${event.orderId}")
        } catch (e: Exception) {
            log.error("Failed to send payment request notification for order ${event.orderId}", e)
        }
    }
}