package com.flick.core.domain.transaction.service

import com.flick.common.error.CustomException
import com.flick.core.domain.transaction.dto.response.TransactionDetailResponse
import com.flick.core.domain.transaction.dto.response.TransactionResponse
import com.flick.core.infra.security.SecurityHolder
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.order.error.OrderError
import com.flick.domain.order.error.OrderItemError
import com.flick.domain.payment.repository.OrderItemRepository
import com.flick.domain.payment.repository.OrderRepository
import com.flick.domain.product.error.ProductError
import com.flick.domain.product.repository.ProductRepository
import com.flick.domain.transaction.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val securityHolder: SecurityHolder,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val boothRepository: BoothRepository,
    private val orderItemRepository: OrderItemRepository,
) {
    suspend fun getMyTransactions(): Flow<TransactionResponse> {
        val userId = securityHolder.getUserId()
        val transactions = transactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)

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

    suspend fun getTransaction(transactionId: Long): TransactionDetailResponse {
        val transaction = transactionRepository.findById(transactionId)
            ?: throw CustomException(OrderError.ORDER_NOT_FOUND)

        val order = orderRepository.findById(transaction.orderId!!)
            ?: throw CustomException(OrderError.ORDER_NOT_FOUND)
        val booth = boothRepository.findById(order.boothId)
            ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)
        val items = orderItemRepository.findAllByOrderId(order.id!!)

        return TransactionDetailResponse(
            id = transaction.id!!,
            type = transaction.type,
            amount = transaction.amount,
            booth = TransactionDetailResponse.Booth(
                id = booth.id!!,
                name = booth.name,
            ),
            items = items.map {
                val product = productRepository.findById(it.productId)
                    ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)
                TransactionDetailResponse.OrderItem(
                    id = it.id!!,
                    product = TransactionDetailResponse.Product(
                        id = product.id!!,
                        name = product.name,
                        price = product.price,
                    ),
                    price = it.price,
                    quantity = it.quantity,
                )
            }.toList(),
            memo = transaction.memo,
            createdAt = transaction.createdAt,
        )
    }
}