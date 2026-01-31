package com.payv.ledger.infrastructure.persistence.mybatis.mapper;

import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransactionMapper {
    boolean existsById(@Param("transactionId") String transactionId);
    TransactionRecord selectTransaction(@Param("transactionId") String transactionId,
                                        @Param("ownerUserId") String ownerUserId);
    int insert(TransactionRecord record);
    int update(TransactionRecord record);
}

