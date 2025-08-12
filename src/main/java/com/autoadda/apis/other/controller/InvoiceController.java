package com.autoadda.apis.other.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.other.entity.AppointmentInvoice;
import com.autoadda.apis.other.request.AppointmentInvoiceRequest;
import com.autoadda.apis.other.request.AppointmentPayRequest;
import com.autoadda.apis.other.response.GenericResponse;
import com.autoadda.apis.other.service.InvoiceService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/prepare")
    public ResponseEntity<GenericResponse> prepareInvoice(@RequestBody AppointmentInvoiceRequest request) {
        AppointmentInvoice invoice = invoiceService.prepareInvoice(request);
        return ResponseEntity.ok(GenericResponse.success("Invoice prepared", invoice));
    }

    @PutMapping("/pay")
    public ResponseEntity<GenericResponse> payInvoice(@RequestBody AppointmentPayRequest request) {
        AppointmentInvoice invoice = invoiceService.updateInvoicePayment(request);
        return ResponseEntity.ok(GenericResponse.success("Invoice updated as paid", invoice));
    }
}
