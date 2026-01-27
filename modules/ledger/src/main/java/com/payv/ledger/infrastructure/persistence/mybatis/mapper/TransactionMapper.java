package com.payv.ledger.infrastructure.persistence.mybatis.mapper;

import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransactionMapper {
    boolean existsById(@Param("id") String id);
    TransactionRecord selectById(@Param("id") String id);
    int insert(TransactionRecord record);
    int update(TransactionRecord record);
}

