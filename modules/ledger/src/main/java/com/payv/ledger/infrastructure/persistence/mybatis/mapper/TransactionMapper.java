package com.payv.ledger.infrastructure.persistence.mybatis.mapper;

import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionMapper {

    boolean existsById(String transactionId);
    TransactionRecord selectById(String transactionId);
    int insert(TransactionRecord record);
    int update(TransactionRecord record);
}
