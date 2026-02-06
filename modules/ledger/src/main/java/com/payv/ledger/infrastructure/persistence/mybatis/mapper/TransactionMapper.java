package com.payv.ledger.infrastructure.persistence.mybatis.mapper;

import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TransactionMapper {

    /** ---- Query ---- */

    List<TransactionRecord> selectList(@Param("ownerUserId") String ownerUserId,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to,
                                       @Param("assetId") String assetId,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    int countList(@Param("ownerUserId") String ownerUserId,
                  @Param("from") LocalDate from,
                  @Param("to") LocalDate to,
                  @Param("assetId") String assetId);

    TransactionRecord selectDetail(@Param("transactionId") String transactionId,
                                   @Param("ownerUserId") String ownerUserId);

    List<String> selectTagIds(@Param("transactionId") String transactionId,
                              @Param("ownerUserId") String ownerUserId);

    /** ---- Command ---- */
    int upsert(TransactionRecord record);

    int deleteByIdAndOwner(@Param("transactionId") String transactionId,
                           @Param("ownerUserId") String ownerUserId);
}
