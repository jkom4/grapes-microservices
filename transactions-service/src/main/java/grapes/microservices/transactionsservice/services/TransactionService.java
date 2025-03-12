package grapes.microservices.transactionsservice.services;

import grapes.microservices.transactionsservice.dto.TransactionDTO;
import grapes.microservices.transactionsservice.mapper.TransactionMapper;
import grapes.microservices.transactionsservice.models.Transaction;
import grapes.microservices.transactionsservice.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionDTO> getTransactionsByClient(String clientId) {
        return transactionRepository.findByClientId(clientId)
                .stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = transactionMapper.toEntity(transactionDTO);
        return transactionMapper.toDTO(transactionRepository.save(transaction));
    }
}

