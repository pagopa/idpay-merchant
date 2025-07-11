package it.gov.pagopa.merchant.controller;

import io.swagger.v3.oas.annotations.Operation;
import it.gov.pagopa.merchant.dto.PointOfSaleListDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/idpay/merchant")
public interface PointOfSaleController {

    @Operation(summary = "Return the point of sales list")
    @GetMapping(value = "/{merchantId}/point-of-sales")
    ResponseEntity<PointOfSaleListDTO> getPointOfSalesList(
            @PathVariable("merchantId") String merchantId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String contactName,
            @PageableDefault(size = 8,
             sort = "franchiseName",
             direction = Sort.Direction.ASC) Pageable pageable);
}
