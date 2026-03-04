package com.payv.ledger.infrastructure.persistence.mybatis.mapper;

import com.payv.ledger.infrastructure.persistence.mybatis.record.LedgerEventOutboxRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface LedgerEventOutboxMapper {

    int insert(LedgerEventOutboxRecord record);

    List<LedgerEventOutboxRecord> selectPending(@Param("now") OffsetDateTime now,
                                                @Param("limit") int limit);

    int markPublished(@Param("outboxId") String outboxId,
                      @Param("publishedAt") OffsetDateTime publishedAt);

    int markRetry(@Param("outboxId") String outboxId,
                  @Param("retryCount") int retryCount,
                  @Param("nextRetryAt") OffsetDateTime nextRetryAt,
                  @Param("lastError") String lastError);
}
