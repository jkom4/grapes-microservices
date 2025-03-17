package grapes.microservices.salesservice.mapper;
import grapes.microservices.salesservice.dto.TransactionDTO;
import grapes.microservices.salesservice.models.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
@Mapper
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

   // @Mapping(target = "transactionDate", source = "transactionDate")
    TransactionDTO toDTO(Transaction transaction);

    //@Mapping(target = "transactionDate", source = "transactionDate")
    Transaction toEntity(TransactionDTO transactionDTO);
}
