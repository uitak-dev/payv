package com.payv.ledger.infrastructure.persistence.mybatis.mapper;

import com.payv.ledger.infrastructure.persistence.mybatis.record.TransferRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TransferMapper {

    List<TransferRecord> selectList(@Param("ownerUserId") String ownerUserId,
                                    @Param("from") LocalDate from, @Param("to") LocalDate to,
                                    @Param("offset") int offset, @Param("limit") int limit);

    int countList(@Param("ownerUserId") String ownerUserId,
                  @Param("from") LocalDate from, @Param("to") LocalDate to);

    TransferRecord selectDetail(@Param("transferId") String transferId,
                                @Param("ownerUserId") String ownerUserId);

    int upsert(TransferRecord record);

    int deleteByIdAndOwner(@Param("transferId") String transferId,
                           @Param("ownerUserId") String ownerUserId);
}
