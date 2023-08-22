package com.woowacamp.soolsool.core.order.service;

import com.woowacamp.soolsool.core.order.code.OrderErrorCode;
import com.woowacamp.soolsool.core.order.domain.Order;
import com.woowacamp.soolsool.core.order.domain.OrderStatus;
import com.woowacamp.soolsool.core.order.domain.vo.OrderStatusType;
import com.woowacamp.soolsool.core.order.dto.response.OrderDetailResponse;
import com.woowacamp.soolsool.core.order.dto.response.OrderListResponse;
import com.woowacamp.soolsool.core.order.repository.OrderRepository;
import com.woowacamp.soolsool.core.order.repository.OrderStatusRepository;
import com.woowacamp.soolsool.core.receipt.domain.Receipt;
import com.woowacamp.soolsool.core.receipt.repository.ReceiptRepository;
import com.woowacamp.soolsool.global.exception.SoolSoolException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ReceiptRepository receiptRepository;

    @Transactional
    public Long saveOrder(final Long memberId, final Long receiptId) {
        final Receipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new SoolSoolException(OrderErrorCode.NOT_EXISTS_RECEIPT));

        final OrderStatus completedOrderStatus = orderStatusRepository
            .findByType(OrderStatusType.COMPLETED)
            .orElseThrow(() -> new SoolSoolException(OrderErrorCode.NOT_EXISTS_ORDER_STATUS));

        final Order order = Order.of(memberId, completedOrderStatus, receipt);

        return orderRepository.save(order).getId();
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse orderDetail(final Long memberId, final Long orderId) {
        final Order order = orderRepository.findOrderById(orderId)
            .orElseThrow(() -> new SoolSoolException(OrderErrorCode.NOT_EXISTS_ORDER));

        validateAccessible(memberId, order);

        return OrderDetailResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderListResponse> orderList(final Long memberId, final Pageable pageable) {
        final Page<Order> orders = orderRepository.findAllByMemberId(memberId, pageable);

        return orders.getContent().stream()
            .map(OrderListResponse::from)
            .collect(Collectors.toUnmodifiableList());
    }

    @Transactional
    public void modifyOrderStatusCancel(final Long memberId, final Long orderId) {
        final Order order = orderRepository.findOrderById(orderId)
            .orElseThrow(() -> new SoolSoolException(OrderErrorCode.NOT_EXISTS_ORDER));

        validateAccessible(memberId, order);

        final OrderStatus cancelOrderStatus = orderStatusRepository.findByType(OrderStatusType.CANCELED)
            .orElseThrow(() -> new SoolSoolException(OrderErrorCode.NOT_EXISTS_ORDER_STATUS));

        order.updateStatus(cancelOrderStatus);
    }

    private void validateAccessible(final Long memberId, final Order order) {
        if (!Objects.equals(memberId, order.getMemberId())) {
            throw new SoolSoolException(OrderErrorCode.ACCESS_DENIED_ORDER);
        }
    }
}
