package com.flick.admin.domain.export.service

import com.flick.domain.booth.entity.BoothEntity
import com.flick.domain.booth.enums.BoothStatus
import com.flick.domain.booth.repository.BoothRepository
import com.flick.domain.order.entity.OrderEntity
import com.flick.domain.order.entity.OrderItemEntity
import com.flick.domain.order.repository.OrderItemRepository
import com.flick.domain.order.repository.OrderRepository
import com.flick.domain.product.entity.ProductEntity
import com.flick.domain.product.repository.ProductRepository
import com.flick.domain.transaction.entity.TransactionEntity
import com.flick.domain.transaction.repository.TransactionRepository
import com.flick.domain.user.entity.UserEntity
import com.flick.domain.user.repository.UserRepository
import com.flick.domain.notification.repository.NotificationRepository
import com.flick.domain.inquiry.repository.InquiryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

@Service
class ExportService(
    private val boothRepository: BoothRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val inquiryRepository: InquiryRepository,
    private val transactionalOperator: TransactionalOperator
) {
    private val logger = LoggerFactory.getLogger(ExportService::class.java)

    suspend fun export(): Resource = transactionalOperator.executeAndAwait {
        logger.info("시작: 축제 결과 내보내기")

        val workbook = XSSFWorkbook()
        val headerStyle = createHeaderStyle(workbook)
        val titleStyle = createTitleStyle(workbook)
        val subHeaderStyle = createSubHeaderStyle(workbook)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        try {
            coroutineScope {
                val booths = async { boothRepository.findAllByStatusOrderByTotalSalesDesc(BoothStatus.APPROVED).toList() }
                val allOrders = async { orderRepository.findByStatusPaid().toList() }
                val allProducts = async { productRepository.findAll().toList() }
                val allOrderItems = async { orderItemRepository.findForPaidOrders().toList() }
                val transactions = async { transactionRepository.findAllByOrderByCreatedAtDesc().toList() }
                val users = async { userRepository.findAll().toList() }
                val notificationStats = async { notificationRepository.countGroupByType() }
                val inquiryStats = async { inquiryRepository.countGroupByCategory() }

                val boothsList = booths.await()
                val ordersList = allOrders.await()
                val productsList = allProducts.await()
                val orderItemsList = allOrderItems.await()
                val transactionsList = transactions.await()
                val usersList = users.await()

                val ordersMapByBooth = ordersList.groupBy { it.boothId }
                val orderItemsMapByOrder = orderItemsList.groupBy { it.orderId }

                val safeOrderMap = ordersMapByBooth.mapKeys { it.key ?: 0L }
                val safeOrderItemMap = orderItemsMapByOrder.mapKeys { it.key ?: 0L }

                createSummarySheet(workbook, titleStyle, headerStyle, boothsList, safeOrderMap, orderItemsList, transactionsList)
                createBoothRankingSheet(workbook, titleStyle, headerStyle, boothsList, safeOrderMap)
                createBoothSheets(workbook, titleStyle, headerStyle, subHeaderStyle, dateFormatter, boothsList, safeOrderMap, safeOrderItemMap, productsList)
                createProductSalesSheet(workbook, titleStyle, headerStyle, boothsList, productsList, orderItemsList)
                createTransactionsSheet(workbook, titleStyle, headerStyle, dateFormatter, transactionsList)
                createUserStatisticsSheet(workbook, titleStyle, headerStyle, usersList, transactionsList)
                createSystemStatisticsSheet(workbook, titleStyle, headerStyle, notificationStats.await(), inquiryStats.await())
            }

            val outputStream = ByteArrayOutputStream()
            workbook.write(outputStream)
            workbook.close()

            ByteArrayResource(outputStream.toByteArray())
        } catch (e: Exception) {
            logger.error("엑셀 내보내기 중 오류 발생", e)
            throw e
        }
    }

    private fun createSummarySheet(
        workbook: XSSFWorkbook,
        titleStyle: XSSFCellStyle,
        headerStyle: XSSFCellStyle,
        booths: List<BoothEntity>,
        ordersMap: Map<Long, List<OrderEntity>>,
        orderItems: List<OrderItemEntity>,
        transactions: List<TransactionEntity>
    ) {
        val sheet = workbook.createSheet("행사 요약")

        val titleRow = sheet.createRow(0)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("학교 축제 판매 결과 요약")
        titleCell.cellStyle = titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 4))

        var rowNum = 2

        val totalOrderCount = ordersMap.values.flatten().size
        val totalOrderAmount = ordersMap.values.flatten().sumOf { it.totalAmount }
        val totalItemCount = orderItems.sumOf { it.quantity }
        val totalChargeAmount = transactions.filter { it.type.toString() == "CHARGE" }.sumOf { it.amount }
        val totalPaymentAmount = transactions.filter { it.type.toString() == "PAYMENT" }.sumOf { it.amount }

        createLabelValueRow(sheet, rowNum++, "총 부스 수", booths.size.toString())
        createLabelValueRow(sheet, rowNum++, "총 주문 수", totalOrderCount.toString())
        createLabelValueRow(sheet, rowNum++, "총 판매 금액", "${formatCurrency(totalOrderAmount)}원")
        createLabelValueRow(sheet, rowNum++, "총 판매 상품 수", totalItemCount.toString())
        createLabelValueRow(sheet, rowNum++, "총 충전 금액", "${formatCurrency(totalChargeAmount)}원")
        createLabelValueRow(sheet, rowNum++, "총 결제 금액", "${formatCurrency(totalPaymentAmount)}원")

        rowNum += 2

        val headerRow = sheet.createRow(rowNum++)
        headerRow.createCell(0).setCellValue("상위 5개 부스")
        headerRow.getCell(0).cellStyle = headerStyle
        headerRow.createCell(1).setCellValue("총 판매액")
        headerRow.getCell(1).cellStyle = headerStyle

        booths.take(5).forEachIndexed { index, booth ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(booth.name)
            row.createCell(1).setCellValue("${formatCurrency(booth.totalSales)}원")
        }

        setColumnWidths(sheet, 5)
    }

    private fun createBoothRankingSheet(
        workbook: XSSFWorkbook,
        titleStyle: XSSFCellStyle,
        headerStyle: XSSFCellStyle,
        booths: List<BoothEntity>,
        ordersMap: Map<Long, List<OrderEntity>>
    ) {
        val sheet = workbook.createSheet("부스 순위")

        val titleRow = sheet.createRow(0)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("부스 판매 순위")
        titleCell.cellStyle = titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 4))

        val headerRow = sheet.createRow(2)
        val headers = listOf("순위", "부스명", "총 판매액", "주문 수", "평균 주문액")

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        booths.forEachIndexed { index, booth ->
            val row = sheet.createRow(index + 3)
            val boothId = booth.id ?: 0L
            val boothOrders = ordersMap[boothId] ?: emptyList()
            val orderCount = boothOrders.size
            val avgOrderAmount = if (orderCount > 0) booth.totalSales.toDouble() / orderCount else 0.0

            row.createCell(0).setCellValue((index + 1).toString())
            row.createCell(1).setCellValue(booth.name)
            row.createCell(2).setCellValue("${formatCurrency(booth.totalSales)}원")
            row.createCell(3).setCellValue(orderCount.toString())
            row.createCell(4).setCellValue("${formatCurrency(avgOrderAmount.toLong())}원")
        }

        setColumnWidths(sheet, headers.size)
    }

    private fun createBoothSheets(
        workbook: XSSFWorkbook,
        titleStyle: XSSFCellStyle,
        headerStyle: XSSFCellStyle,
        subHeaderStyle: XSSFCellStyle,
        dateFormatter: DateTimeFormatter,
        booths: List<BoothEntity>,
        ordersMap: Map<Long, List<OrderEntity>>,
        orderItemsMap: Map<Long, List<OrderItemEntity>>,
        products: List<ProductEntity>
    ) {
        for (booth in booths) {
            val boothId = booth.id ?: continue

            val sheetName = booth.name.take(30)
            val sheet = workbook.createSheet(sheetName)

            val titleRow = sheet.createRow(0)
            val titleCell = titleRow.createCell(0)
            titleCell.setCellValue("${booth.name} 판매 내역")
            titleCell.cellStyle = titleStyle
            sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 5))

            var rowNum = 2

            val boothOrders = ordersMap[boothId] ?: emptyList()
            val totalSales = booth.totalSales
            val orderCount = boothOrders.size
            val avgOrderAmount = if (orderCount > 0) totalSales.toDouble() / orderCount else 0.0
            val boothProducts = products.filter { it.boothId == boothId }
            val productCount = boothProducts.size

            createLabelValueRow(sheet, rowNum++, "부스명", booth.name)
            createLabelValueRow(sheet, rowNum++, "설명", booth.description ?: "")
            createLabelValueRow(sheet, rowNum++, "총 판매액", "${formatCurrency(totalSales)}원")
            createLabelValueRow(sheet, rowNum++, "총 주문 수", orderCount.toString())
            createLabelValueRow(sheet, rowNum++, "평균 주문액", "${formatCurrency(avgOrderAmount.toLong())}원")
            createLabelValueRow(sheet, rowNum++, "판매 상품 수", productCount.toString())

            rowNum += 2

            val productHeaderRow = sheet.createRow(rowNum++)
            val productCell = productHeaderRow.createCell(0)
            productCell.setCellValue("상품별 판매량")
            productCell.cellStyle = subHeaderStyle
            sheet.addMergedRegion(CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3))

            val productTableHeader = sheet.createRow(rowNum++)
            listOf("상품명", "가격", "판매량", "총 판매액").forEachIndexed { index, header ->
                val cell = productTableHeader.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            val productSalesMap = mutableMapOf<Long, Pair<Int, Long>>()
            for (order in boothOrders) {
                val orderId = order.id ?: continue
                val items = orderItemsMap[orderId] ?: continue
                for (item in items) {
                    val productId = item.productId
                    val current = productSalesMap.getOrDefault(productId, 0 to 0L)
                    productSalesMap[productId] = current.first + item.quantity to current.second + (item.price * item.quantity)
                }
            }

            boothProducts.forEach { product ->
                val productId = product.id ?: 0L
                val (quantity, totalSalesForProduct) = productSalesMap.getOrDefault(productId, 0 to 0L)

                if (quantity > 0) {
                    val row = sheet.createRow(rowNum++)
                    row.createCell(0).setCellValue(product.name)
                    row.createCell(1).setCellValue("${formatCurrency(product.price)}원")
                    row.createCell(2).setCellValue(quantity.toString())
                    row.createCell(3).setCellValue("${formatCurrency(totalSalesForProduct)}원")
                }
            }

            rowNum += 2

            val orderHeaderRow = sheet.createRow(rowNum++)
            val orderCell = orderHeaderRow.createCell(0)
            orderCell.setCellValue("주문 내역")
            orderCell.cellStyle = subHeaderStyle
            sheet.addMergedRegion(CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5))

            val orderTableHeader = sheet.createRow(rowNum++)
            listOf("주문 ID", "주문 번호", "주문 금액", "사용자 ID", "상태", "결제 시간").forEachIndexed { index, header ->
                val cell = orderTableHeader.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            boothOrders.forEach { order ->
                val row = sheet.createRow(rowNum++)
                row.createCell(0).setCellValue(order.id?.toString() ?: "")
                row.createCell(1).setCellValue(order.boothOrderNumber.toString())
                row.createCell(2).setCellValue("${formatCurrency(order.totalAmount)}원")
                row.createCell(3).setCellValue(order.userId?.toString() ?: "")
                row.createCell(4).setCellValue(order.status.toString())
                row.createCell(5).setCellValue(order.paidAt?.format(dateFormatter) ?: "")
            }

            setColumnWidths(sheet, 6)
        }
    }

    private fun createProductSalesSheet(
        workbook: XSSFWorkbook,
        titleStyle: XSSFCellStyle,
        headerStyle: XSSFCellStyle,
        booths: List<BoothEntity>,
        products: List<ProductEntity>,
        orderItems: List<OrderItemEntity>
    ) {
        val sheet = workbook.createSheet("상품별 판매량")

        val titleRow = sheet.createRow(0)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("상품별 판매량")
        titleCell.cellStyle = titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 5))

        val headerRow = sheet.createRow(2)
        val headers = listOf("순위", "부스명", "상품명", "가격", "판매량", "총 판매액")

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        val productSalesMap = orderItems
            .groupBy { it.productId }
            .mapValues { (_, items) ->
                items.sumOf { it.quantity } to items.sumOf { it.price * it.quantity }
            }

        val productRankings = products
            .mapNotNull { product ->
                val productId = product.id ?: return@mapNotNull null
                val boothId = product.boothId
                val (quantity, totalSales) = productSalesMap[productId] ?: (0 to 0L)
                if (quantity <= 0) return@mapNotNull null

                val booth = booths.find { it.id == boothId }
                ProductSalesData(product, booth?.name ?: "알 수 없음", quantity, totalSales)
            }
            .sortedByDescending { it.quantity }

        productRankings.forEachIndexed { index, data ->
            val row = sheet.createRow(index + 3)

            row.createCell(0).setCellValue((index + 1).toString())
            row.createCell(1).setCellValue(data.boothName)
            row.createCell(2).setCellValue(data.product.name)
            row.createCell(3).setCellValue("${formatCurrency(data.product.price)}원")
            row.createCell(4).setCellValue(data.quantity.toString())
            row.createCell(5).setCellValue("${formatCurrency(data.totalSales)}원")
        }

        setColumnWidths(sheet, headers.size)
    }

    private fun createTransactionsSheet(
        workbook: XSSFWorkbook,
        titleStyle: XSSFCellStyle,
        headerStyle: XSSFCellStyle,
        dateFormatter: DateTimeFormatter,
        transactions: List<TransactionEntity>
    ) {
        val sheet = workbook.createSheet("거래 내역")

        val titleRow = sheet.createRow(0)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("거래 내역")
        titleCell.cellStyle = titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 6))

        val headerRow = sheet.createRow(2)
        val headers = listOf("거래 ID", "사용자 ID", "유형", "금액", "거래 후 잔액", "메모", "거래 시간")

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 3)

            row.createCell(0).setCellValue(transaction.id?.toString() ?: "")
            row.createCell(1).setCellValue(transaction.userId.toString())
            row.createCell(2).setCellValue(transaction.type.toString())
            row.createCell(3).setCellValue("${formatCurrency(transaction.amount)}원")
            row.createCell(4).setCellValue("${formatCurrency(transaction.balanceAfter)}원")
            row.createCell(5).setCellValue(transaction.memo ?: "")
            row.createCell(6).setCellValue(transaction.createdAt.format(dateFormatter))
        }

        setColumnWidths(sheet, headers.size)
    }

    private fun createUserStatisticsSheet(
        workbook: XSSFWorkbook,
        titleStyle: XSSFCellStyle,
        headerStyle: XSSFCellStyle,
        users: List<UserEntity>,
        transactions: List<TransactionEntity>
    ) {
        val sheet = workbook.createSheet("사용자 통계")

        val titleRow = sheet.createRow(0)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("사용자 통계")
        titleCell.cellStyle = titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 6))

        val headerRow = sheet.createRow(2)
        val headers = listOf("사용자 ID", "이름", "현재 잔액", "총 충전액", "총 결제액", "결제 횟수", "평균 결제액")

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        val transactionsByUser = transactions.groupBy { it.userId }

        var rowIndex = 3
        users.forEach { user ->
            val userId = user.id ?: return@forEach
            val userTransactions = transactionsByUser[userId] ?: emptyList()
            val charges = userTransactions.filter { it.type.toString() == "CHARGE" }
            val payments = userTransactions.filter { it.type.toString() == "PAYMENT" }

            val totalCharge = charges.sumOf { it.amount }
            val totalPayment = payments.sumOf { it.amount }
            val paymentCount = payments.size
            val avgPayment = if (paymentCount > 0) totalPayment.toDouble() / paymentCount else 0.0

            val row = sheet.createRow(rowIndex++)

            row.createCell(0).setCellValue(userId.toString())
            row.createCell(1).setCellValue(user.name)
            row.createCell(2).setCellValue("${formatCurrency(user.balance)}원")
            row.createCell(3).setCellValue("${formatCurrency(totalCharge)}원")
            row.createCell(4).setCellValue("${formatCurrency(totalPayment)}원")
            row.createCell(5).setCellValue(paymentCount.toString())
            row.createCell(6).setCellValue("${formatCurrency(avgPayment.toLong())}원")
        }

        setColumnWidths(sheet, headers.size)
    }

    private fun createSystemStatisticsSheet(
        workbook: XSSFWorkbook,
        titleStyle: XSSFCellStyle,
        headerStyle: XSSFCellStyle,
        notificationStats: List<Map<String, Any>>,
        inquiryStats: List<Map<String, Any>>
    ) {
        val sheet = workbook.createSheet("시스템 통계")

        val titleRow = sheet.createRow(0)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("시스템 통계")
        titleCell.cellStyle = titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 2))

        var rowNum = 2

        val notificationHeaderRow = sheet.createRow(rowNum++)
        val notificationCell = notificationHeaderRow.createCell(0)
        notificationCell.setCellValue("알림 유형별 개수")
        notificationCell.cellStyle = headerStyle
        sheet.addMergedRegion(CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2))

        val notificationTableHeader = sheet.createRow(rowNum++)
        listOf("알림 유형", "개수").forEachIndexed { index, header ->
            val cell = notificationTableHeader.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        notificationStats.forEach { stat ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(stat["type"]?.toString() ?: "")
            row.createCell(1).setCellValue(stat["count"]?.toString() ?: "0")
        }

        rowNum += 2

        val inquiryHeaderRow = sheet.createRow(rowNum++)
        val inquiryCell = inquiryHeaderRow.createCell(0)
        inquiryCell.setCellValue("문의 유형별 개수")
        inquiryCell.cellStyle = headerStyle
        sheet.addMergedRegion(CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2))

        val inquiryTableHeader = sheet.createRow(rowNum++)
        listOf("문의 유형", "개수").forEachIndexed { index, header ->
            val cell = inquiryTableHeader.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        inquiryStats.forEach { stat ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(stat["category"]?.toString() ?: "")
            row.createCell(1).setCellValue(stat["count"]?.toString() ?: "0")
        }

        setColumnWidths(sheet, 3)
    }

    private fun createLabelValueRow(sheet: XSSFSheet, rowNum: Int, label: String, value: String) {
        val row = sheet.createRow(rowNum)
        row.createCell(0).setCellValue(label)
        row.createCell(1).setCellValue(value)
    }

    private fun setColumnWidths(sheet: XSSFSheet, columnCount: Int) {
        for (i in 0 until columnCount) {
            sheet.setColumnWidth(i, 15 * 256)
        }
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()

        font.bold = true
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.borderBottom = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER

        return style
    }

    private fun createTitleStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()

        font.bold = true
        font.fontHeightInPoints = 14
        style.setFont(font)
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER

        return style
    }

    private fun createSubHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()

        font.bold = true
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.LIGHT_GREEN.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.CENTER

        return style
    }

    private fun formatCurrency(amount: Long): String {
        return String.format("%,d", amount)
    }

    data class ProductSalesData(
        val product: ProductEntity,
        val boothName: String,
        val quantity: Int,
        val totalSales: Long
    )
}