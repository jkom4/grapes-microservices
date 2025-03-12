package grapes.microservices.transactionsservice.mapper;
import grapes.microservices.transactionsservice.dto.TransactionDTO;
import grapes.microservices.transactionsservice.models.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
@Mapper
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(target = "transactionDate", source = "transactionDate")
    TransactionDTO toDTO(Transaction transaction);

    @Mapping(target = "transactionDate", source = "transactionDate")
    Transaction toEntity(TransactionDTO transactionDTO);
}
