package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.TransactionDTO;
import grapes.microservices.salesservice.services.TransactionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value  = "/clm/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByClient(@PathVariable String clientId) {
        return ResponseEntity.ok(transactionService.getTransactionsByClient(clientId));
    }

    @PostMapping("/")
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionDTO transactionDTO) {
        return ResponseEntity.ok(transactionService.createTransaction(transactionDTO));
    }
}
