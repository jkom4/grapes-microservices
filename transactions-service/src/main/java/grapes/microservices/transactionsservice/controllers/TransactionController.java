package grapes.microservices.transactionsservice.controllers;

import grapes.microservices.transactionsservice.dto.TransactionDTO;
import grapes.microservices.transactionsservice.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
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
