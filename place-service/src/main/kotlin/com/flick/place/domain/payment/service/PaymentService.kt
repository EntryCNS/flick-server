package com.flick.place.domain.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.flick.common.error.CustomException
import com.flick.common.utils.logger
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.order.entity.OrderEntity
import com.flick.domain.order.enums.OrderStatus
import com.flick.domain.order.error.OrderError
import com.flick.domain.payment.entity.PaymentRequestEntity
import com.flick.domain.payment.enums.PaymentMethod
import com.flick.domain.payment.repository.OrderRepository
import com.flick.domain.payment.repository.PaymentRequestRepository
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import com.flick.place.domain.payment.dto.*
import com.flick.place.infra.security.SecurityHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val orderRepository: OrderRepository,
    private val paymentRequestRepository: PaymentRequestRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val boothRepository: BoothRepository,
    private val objectMapper: ObjectMapper,
    private val securityHolder: SecurityHolder,
    private val userRepository: UserRepository,
) {
    private val log = logger()

    suspend fun createQrPayment(request: CreateQrPaymentRequest): CreateQrPaymentResponse {
        val order = getPendingOrder(request.orderId)
        val token = generatePaymentToken()

        val paymentRequest = paymentRequestRepository.save(
            PaymentRequestEntity(
                orderId = order.id!!,
                method = PaymentMethod.QR_CODE,
                token = token,
            )
        )

        return CreateQrPaymentResponse(
            id = paymentRequest.id!!,
            token = token
        )
    }

    suspend fun createStudentIdPayment(request: CreateStudentIdPaymentRequest): CreateStudentIdPaymentResponse {
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
        )
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        sendPaymentRequestNotification(
            userId = user.id!!,
            orderId = order.id!!,
            boothName = booth.name,
            totalAmount = order.totalAmount,
            token = token
        )

        return CreateStudentIdPaymentResponse(
            id = paymentRequest.id!!,
        )
    }

    private suspend fun getPendingOrder(orderId: Long): OrderEntity {
        val order = orderRepository.findByIdAndBoothId(orderId, securityHolder.getBoothId())
            ?: throw CustomException(OrderError.ORDER_NOT_FOUND)

        if (order.status != OrderStatus.PENDING) throw CustomException(OrderError.ORDER_NOT_PENDING)

        return order
    }

    private fun generatePaymentToken() = (1..32).map { (('a'..'z') + ('1'..'9')).random() }.joinToString("")

    private suspend fun sendPaymentRequestNotification(
        userId: Long,
        orderId: Long,
        boothName: String,
        totalAmount: Long,
        token: String
    ) {
        val event = PaymentRequestEventDto(
            userId = userId,
            orderId = orderId,
            boothName = boothName,
            totalAmount = totalAmount,
            token = token
        )

        try {
            val eventJson = objectMapper.writeValueAsString(event)

            withContext(Dispatchers.IO) {
                kafkaTemplate.send("payment-request", eventJson)
            }

            log.info("Payment request notification sent for order $orderId")
        } catch (e: Exception) {
            log.error("Failed to send payment request notification for order $orderId", e)
        }
    }
}