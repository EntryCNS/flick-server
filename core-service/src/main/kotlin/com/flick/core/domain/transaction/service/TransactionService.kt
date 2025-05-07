package com.flick.core.domain.transaction.service

import com.flick.common.error.CustomException
import com.flick.core.domain.transaction.dto.response.TransactionResponse
import com.flick.core.infra.security.SecurityHolder
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.order.error.OrderError
import com.flick.domain.order.error.OrderItemError
import com.flick.domain.payment.error.ProductError
import com.flick.domain.payment.repository.OrderItemRepository
import com.flick.domain.payment.repository.OrderRepository
import com.flick.domain.payment.repository.ProductRepository
import com.flick.domain.transaction.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val securityHolder: SecurityHolder,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val boothRepository: BoothRepository,
    private val orderItemRepository: OrderItemRepository
) {
    suspend fun getMyTransactions(): Flow<TransactionResponse> {
        val userId = securityHolder.getUserId()
        val transactions = transactionRepository.findAllByUserId(userId)

        return transactions.map {
            val order = orderRepository.findById(it.orderId!!)
                ?: throw CustomException(OrderError.ORDER_NOT_FOUND)
            val booth = boothRepository.findById(order.boothId)
                ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)
            val item = orderItemRepository.findFirstByOrderId(order.id!!)
                ?: throw CustomException(OrderItemError.ORDER_ITEM_NOT_FOUND)
            val product = productRepository.findById(item.productId)
                ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)

            TransactionResponse(
                id = it.id!!,
                type = it.type,
                amount = it.amount,
                booth = TransactionResponse.Booth(
                    name = booth.name,
                ),
                product = TransactionResponse.Product(
                    name = product.name
                ),
                memo = it.memo,
                createdAt = it.createdAt,
            )
        }
    }
}