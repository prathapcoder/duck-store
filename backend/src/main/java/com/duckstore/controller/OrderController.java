package com.duckstore.controller;

import com.duckstore.dto.OrderQuoteResponse;
import com.duckstore.dto.OrderRequest;
import com.duckstore.service.OrderCommand;
import com.duckstore.service.OrderQuoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store")
public class OrderController {

    private final OrderQuoteService quotes;

    public OrderController(OrderQuoteService quotes) {
        this.quotes = quotes;
    }

    /** Prices an order and decides its packaging. It does not reserve or reduce stock. */
    @PostMapping("/orders")
    public OrderQuoteResponse order(@Valid @RequestBody OrderRequest request) {
        OrderCommand command = new OrderCommand(request.color(), request.size(), request.quantity(),
                request.destinationCountry(), request.shippingMode());
        return OrderQuoteResponse.from(quotes.quote(command));
    }
}
