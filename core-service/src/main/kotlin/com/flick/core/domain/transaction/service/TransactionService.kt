package com.flick.core.domain.transaction.service

import com.flick.common.error.CustomException
import com.flick.core.domain.transaction.dto.response.TransactionDetailResponse
import com.flick.core.domain.transaction.dto.response.TransactionResponse
import com.flick.core.infra.security.SecurityHolder
import com.flick.domain.booth.error.BoothError
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.order.error.OrderError
import com.flick.domain.order.entity.OrderItemEntity
import com.flick.domain.order.repository.OrderItemRepository
import com.flick.domain.order.repository.OrderRepository
import com.flick.domain.product.error.ProductError
import com.flick.domain.product.repository.ProductRepository
import com.flick.domain.transaction.entity.TransactionEntity
import com.flick.domain.transaction.enums.TransactionType
import com.flick.domain.transaction.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val securityHolder: SecurityHolder,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val boothRepository: BoothRepository,
    private val orderItemRepository: OrderItemRepository,
    private val transactionalOperator: TransactionalOperator
) {
    suspend fun getMyTransactions(): Flow<TransactionResponse> {
        val userId = securityHolder.getUserId()
        val transactions = transactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)

        return transactionalOperator.executeAndAwait {
            transactions.map { transaction ->
                when (transaction.type) {
                    TransactionType.CHARGE -> createChargeTransactionResponse(transaction)
                    else -> createPaymentTransactionResponse(transaction)
                }
            }
        }
    }

    suspend fun getTransaction(transactionId: Long): TransactionDetailResponse = transactionalOperator.executeAndAwait {
        val transaction = transactionRepository.findById(transactionId)
            ?: throw CustomException(OrderError.ORDER_NOT_FOUND)

        val order = findOrderById(transaction.orderId!!)
        val booth = findBoothById(order.boothId)
        val items = orderItemRepository.findAllByOrderId(order.id!!)

        TransactionDetailResponse(
            id = transaction.id!!,
            type = transaction.type,
            amount = transaction.amount,
            booth = TransactionDetailResponse.Booth(
                id = booth.id!!,
                name = booth.name,
            ),
            items = items.map { item ->
                val product = findProductById(item.productId)
                TransactionDetailResponse.OrderItem(
                    id = item.id!!,
                    product = TransactionDetailResponse.Product(
                        id = product.id!!,
                        name = product.name,
                        price = product.price,
                    ),
                    price = item.price,
                    quantity = item.quantity,
                )
            }.toList(),
            memo = transaction.memo,
            createdAt = transaction.createdAt,
        )
    }

    private fun createChargeTransactionResponse(
        transaction: TransactionEntity
    ) = TransactionResponse(
        id = transaction.id!!,
        type = transaction.type,
        amount = transaction.amount,
        createdAt = transaction.createdAt,
    )

    private suspend fun createPaymentTransactionResponse(
        transaction: TransactionEntity
    ): TransactionResponse {
        val order = findOrderById(transaction.orderId!!)
        val booth = findBoothById(order.boothId)
        val orderItems = orderItemRepository.findAllByOrderId(order.id!!).toList()

        return TransactionResponse(
            id = transaction.id!!,
            type = transaction.type,
            amount = transaction.amount,
            booth = TransactionResponse.Booth(name = booth.name),
            product = createProductInfo(orderItems),
            memo = transaction.memo,
            createdAt = transaction.createdAt,
        )
    }

    private suspend fun createProductInfo(orderItems: List<OrderItemEntity>): TransactionResponse.Product = when {
        orderItems.isEmpty() -> TransactionResponse.Product(name = "제품 정보 없음")
        orderItems.size == 1 -> {
            val product = findProductById(orderItems.first().productId)
            TransactionResponse.Product(name = product.name)
        }
        else -> {
            val firstProduct = findProductById(orderItems.first().productId)
            TransactionResponse.Product(name = "${firstProduct.name} 외 ${orderItems.size - 1}개")
        }
    }

    private suspend fun findOrderById(orderId: Long) =
        orderRepository.findById(orderId) ?: throw CustomException(OrderError.ORDER_NOT_FOUND)

    private suspend fun findBoothById(boothId: Long) =
        boothRepository.findById(boothId) ?: throw CustomException(BoothError.BOOTH_NOT_FOUND)

    private suspend fun findProductById(productId: Long) =
        productRepository.findById(productId) ?: throw CustomException(ProductError.PRODUCT_NOT_FOUND)
}