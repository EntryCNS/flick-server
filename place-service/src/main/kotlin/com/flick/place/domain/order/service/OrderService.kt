package com.flick.place.domain.order.service

import com.flick.common.error.CustomException
import com.flick.domain.order.entity.Order
import com.flick.domain.order.entity.OrderItem
import com.flick.domain.order.enums.OrderStatus
import com.flick.domain.order.error.OrderError
import com.flick.domain.order.repository.OrderItemRepository
import com.flick.domain.order.repository.OrderRepository
import com.flick.domain.payment.entity.PaymentRequest
import com.flick.domain.payment.enums.PaymentStatus
import com.flick.domain.payment.enums.RequestMethod
import com.flick.domain.payment.repository.PaymentRequestRepository
import com.flick.domain.product.repository.ProductRepository
import com.flick.place.domain.kiosk.service.KioskSessionService
import com.flick.place.domain.order.dto.CreateOrderRequest
import com.flick.place.domain.order.dto.OrderItemResponse
import com.flick.place.domain.order.dto.OrderResponse
import com.flick.place.domain.order.dto.PaymentRequestResponse
import com.flick.place.infra.security.JwtHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val paymentRequestRepository: PaymentRequestRepository,
    private val kioskSessionService: KioskSessionService,
    private val jwtHolder: JwtHolder
) {
    suspend fun getOrders(status: OrderStatus? = null): Flow<OrderResponse> =
        (status?.let { orderRepository.findByBoothIdAndStatus(jwtHolder.getBoothId(), it) }
            ?: orderRepository.findByBoothId(jwtHolder.getBoothId()))
            .map { OrderResponse.of(it, getOrderItemsWithProduct(it.id!!)) }

    suspend fun getTodayOrders(): Flow<OrderResponse> =
        orderRepository.findTodayOrdersByBoothId(jwtHolder.getBoothId())
            .map { OrderResponse.of(it, getOrderItemsWithProduct(it.id!!)) }

    suspend fun getOrder(orderId: Long): OrderResponse {
        val order = orderRepository.findById(orderId) ?: throw CustomException(OrderError.ORDER_NOT_FOUND)
        if (order.boothId != jwtHolder.getBoothId()) throw CustomException(OrderError.BOOTH_MISMATCH)
        return OrderResponse.of(order, getOrderItemsWithProduct(orderId))
    }

    @Transactional
    suspend fun createOrder(request: CreateOrderRequest): OrderResponse {
        if (request.items.isEmpty()) throw CustomException(OrderError.EMPTY_ORDER_ITEMS)

        val boothId = jwtHolder.getBoothId()
        val orderItems = request.items.map { item ->
            val product = productRepository.findById(item.productId) ?: throw CustomException(OrderError.PRODUCT_NOT_FOUND)
            if (product.boothId != boothId) throw CustomException(OrderError.BOOTH_MISMATCH)
            if (product.stock < item.quantity) throw CustomException(OrderError.INSUFFICIENT_STOCK)
            Triple(product.id!!, item.quantity, product.price * item.quantity)
        }

        val savedOrder = orderRepository.save(Order(
            boothId = boothId,
            kioskSessionId = kioskSessionService.getCurrentSessionId(),
            totalAmount = orderItems.sumOf { it.third },
            status = OrderStatus.PENDING
        ))

        orderItems.forEach { (productId, quantity, _) ->
            orderItemRepository.save(OrderItem(
                orderId = savedOrder.id!!,
                productId = productId,
                quantity = quantity
            ))
        }

        return OrderResponse.of(savedOrder, getOrderItemsWithProduct(savedOrder.id!!))
    }

    @Transactional
    suspend fun createQrPaymentRequest(orderId: Long): PaymentRequestResponse =
        createPaymentRequest(orderId, RequestMethod.QR_CODE)

    @Transactional
    suspend fun createStudentIdPaymentRequest(orderId: Long, studentId: String): PaymentRequestResponse =
        createPaymentRequest(orderId, RequestMethod.STUDENT_ID, studentId)

    suspend fun getPaymentRequest(orderId: Long): PaymentRequestResponse? {
        val order = orderRepository.findById(orderId) ?: throw CustomException(OrderError.ORDER_NOT_FOUND)
        if (order.boothId != jwtHolder.getBoothId()) throw CustomException(OrderError.BOOTH_MISMATCH)
        return paymentRequestRepository.findLatestPendingByOrderId(orderId)?.let { PaymentRequestResponse.of(it) }
    }

    private suspend fun createPaymentRequest(
        orderId: Long,
        requestMethod: RequestMethod,
        studentId: String? = null
    ): PaymentRequestResponse {
        val order = orderRepository.findById(orderId) ?: throw CustomException(OrderError.ORDER_NOT_FOUND)
        if (order.boothId != jwtHolder.getBoothId()) throw CustomException(OrderError.BOOTH_MISMATCH)
        if (order.status != OrderStatus.PENDING) throw CustomException(OrderError.INVALID_ORDER_STATUS)

        paymentRequestRepository.findLatestPendingByOrderId(orderId)?.let {
            paymentRequestRepository.save(it.copy(status = PaymentStatus.EXPIRED))
        }

        val savedRequest = paymentRequestRepository.save(PaymentRequest(
            orderId = orderId,
            requestCode = (1..8).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString(""),
            requestMethod = requestMethod,
            studentId = studentId,
            status = PaymentStatus.PENDING
        ))

        return PaymentRequestResponse.of(savedRequest)
    }

    private suspend fun getOrderItemsWithProduct(orderId: Long): List<OrderItemResponse> =
        orderItemRepository.findByOrderId(orderId).toList().mapNotNull { item ->
            productRepository.findById(item.productId)?.let { product ->
                OrderItemResponse(
                    id = item.id!!,
                    productId = product.id!!,
                    name = product.name,
                    price = product.price,
                    quantity = item.quantity
                )
            }
        }
}