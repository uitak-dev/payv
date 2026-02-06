package com.payv.ledger.infrastructure.persistence.mybatis.mapper;

import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionTagRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionTagMapper {

    void deleteByTransactionId(@Param("transactionId") String transactionId);
    void deleteByTransactionIdAndTagIds(@Param("transactionId") String transactionId,
                                        @Param("tagIds") List<String> tagIds);
    void insertTags(@Param("records") List<TransactionTagRecord> records);

    List<TransactionTagRecord> selectByTransactionId(@Param("transactionId") String transactionId);
}
