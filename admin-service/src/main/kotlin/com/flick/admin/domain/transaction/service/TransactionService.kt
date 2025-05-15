package com.flick.admin.domain.transaction.service

import com.flick.admin.domain.transaction.dto.response.TransactionDetailResponse
import com.flick.admin.domain.transaction.dto.response.TransactionResponse
import com.flick.common.dto.Page
import com.flick.common.error.CustomException
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
import com.flick.domain.transaction.error.TransactionError
import com.flick.domain.transaction.repository.TransactionRepository
import com.flick.domain.user.error.UserError
import com.flick.domain.user.repository.UserRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.LocalDate
import kotlin.collections.map

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val boothRepository: BoothRepository,
    private val orderItemRepository: OrderItemRepository,
    private val transactionalOperator: TransactionalOperator,
    private val userRepository: UserRepository
) {
    suspend fun getTransactions(
        page: Int,
        size: Int,
        userId: Long?,
        type: TransactionType?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Page<TransactionResponse> {
        val typeStr = type?.name
        val offset = (page - 1) * size

        val transactions = transactionRepository.findByFilters(
            userId, typeStr, startDate, endDate, size, offset
        ).map { transaction ->
            when (transaction.type) {
                TransactionType.CHARGE -> createChargeTransactionResponse(transaction)
                TransactionType.PAYMENT -> createPaymentTransactionResponse(transaction)
                TransactionType.TRANSFER_IN -> createTransferInTransactionResponse(transaction)
                TransactionType.TRANSFER_OUT -> createTransferOutTransactionResponse(transaction)
            }
        }.toList()

        val totalElements = transactionRepository.countByFilters(userId, typeStr, startDate, endDate)

        return Page.of(
            content = transactions,
            pageNumber = page,
            pageSize = size,
            totalElements = totalElements
        )
    }

    suspend fun getTransaction(transactionId: Long): TransactionDetailResponse = transactionalOperator.executeAndAwait {
        val transaction = transactionRepository.findById(transactionId)
            ?: throw CustomException(TransactionError.TRANSACTION_NOT_FOUND)

        when (transaction.type) {
            TransactionType.CHARGE -> createChargeTransactionDetailResponse(transaction)
            TransactionType.PAYMENT -> createPaymentTransactionDetailResponse(transaction)
            TransactionType.TRANSFER_IN -> createTransferInTransactionDetailResponse(transaction)
            TransactionType.TRANSFER_OUT -> createTransferOutTransactionDetailResponse(transaction)
        }
    }

    private suspend fun createChargeTransactionResponse(
        transaction: TransactionEntity
    ) = TransactionResponse(
        id = transaction.id!!,
        user = TransactionResponse.User(
            id = transaction.userId,
            name = getUserName(transaction.userId)
        ),
        type = transaction.type,
        amount = transaction.amount,
        balanceAfter = transaction.balanceAfter,
        createdAt = transaction.createdAt
    )

    private suspend fun createPaymentTransactionResponse(
        transaction: TransactionEntity
    ): TransactionResponse {
        val order = transaction.orderId?.let { findOrderById(it) }
        val booth = order?.let { findBoothById(it.boothId) }
        val orderItems = transaction.orderId?.let { orderItemRepository.findAllByOrderId(it).toList() } ?: emptyList()

        return TransactionResponse(
            id = transaction.id!!,
            user = TransactionResponse.User(
                id = transaction.userId,
                name = getUserName(transaction.userId)
            ),
            type = transaction.type,
            amount = transaction.amount,
            balanceAfter = transaction.balanceAfter,
            booth = booth?.let { TransactionResponse.Booth(name = it.name) },
            product = createProductInfo(orderItems),
            memo = transaction.memo,
            createdAt = transaction.createdAt
        )
    }

    private fun createChargeTransactionDetailResponse(
        transaction: TransactionEntity
    ) = TransactionDetailResponse(
        id = transaction.id!!,
        userId = transaction.userId,
        type = transaction.type,
        amount = transaction.amount,
        balanceAfter = transaction.balanceAfter,
        orderId = transaction.orderId,
        adminId = transaction.adminId,
        memo = transaction.memo,
        createdAt = transaction.createdAt
    )

    private suspend fun createPaymentTransactionDetailResponse(
        transaction: TransactionEntity
    ): TransactionDetailResponse {
        val order = transaction.orderId?.let { findOrderById(it) }
        val booth = order?.let { findBoothById(it.boothId) }
        val items = transaction.orderId?.let { orderItemRepository.findAllByOrderId(it).toList() } ?: emptyList()

        return TransactionDetailResponse(
            id = transaction.id!!,
            userId = transaction.userId,
            type = transaction.type,
            amount = transaction.amount,
            balanceAfter = transaction.balanceAfter,
            orderId = transaction.orderId,
            adminId = transaction.adminId,
            memo = transaction.memo,
            createdAt = transaction.createdAt,
            booth = booth?.let {
                TransactionDetailResponse.Booth(
                    id = it.id!!,
                    name = it.name
                )
            },
            items = items.map { item ->
                val product = findProductById(item.productId)
                TransactionDetailResponse.OrderItem(
                    id = item.id!!,
                    product = TransactionDetailResponse.Product(
                        id = product.id!!,
                        name = product.name,
                        price = product.price
                    ),
                    price = item.price,
                    quantity = item.quantity
                )
            }
        )
    }

    private suspend fun createTransferOutTransactionResponse(
        transaction: TransactionEntity
    ) = TransactionResponse(
        id = transaction.id!!,
        user = TransactionResponse.User(
            id = transaction.userId,
            name = getUserName(transaction.userId)
        ),
        type = transaction.type,
        amount = transaction.amount,
        balanceAfter = transaction.balanceAfter,
        memo = transaction.memo,
        createdAt = transaction.createdAt
    )

    private suspend fun createTransferInTransactionResponse(
        transaction: TransactionEntity
    ) = TransactionResponse(
        id = transaction.id!!,
        user = TransactionResponse.User(
            id = transaction.userId,
            name = getUserName(transaction.userId)
        ),
        type = transaction.type,
        amount = transaction.amount,
        balanceAfter = transaction.balanceAfter,
        memo = transaction.memo,
        createdAt = transaction.createdAt
    )

    private suspend fun createTransferInTransactionDetailResponse(
        transaction: TransactionEntity
    ) = TransactionDetailResponse(
        id = transaction.id!!,
        userId = transaction.userId,
        type = transaction.type,
        amount = transaction.amount,
        balanceAfter = transaction.balanceAfter,
        createdAt = transaction.createdAt
    )

    private suspend fun createTransferOutTransactionDetailResponse(
        transaction: TransactionEntity
    ) = TransactionDetailResponse(
        id = transaction.id!!,
        userId = transaction.userId,
        type = transaction.type,
        amount = transaction.amount,
        balanceAfter = transaction.balanceAfter,
        createdAt = transaction.createdAt
    )

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

    private suspend fun getUserName(userId: Long) =
        (userRepository.findById(userId) ?: throw CustomException(UserError.USER_NOT_FOUND)).name
}