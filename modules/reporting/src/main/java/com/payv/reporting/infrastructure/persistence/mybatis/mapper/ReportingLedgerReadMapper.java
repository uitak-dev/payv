package com.payv.reporting.infrastructure.persistence.mybatis.mapper;

import com.payv.reporting.infrastructure.persistence.mybatis.record.AmountByIdRecord;
import com.payv.reporting.infrastructure.persistence.mybatis.record.RecentTransactionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReportingLedgerReadMapper {

    Long sumAmountByType(@Param("ownerUserId") String ownerUserId,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to,
                         @Param("transactionType") String transactionType);

    List<AmountByIdRecord> sumExpenseByAsset(@Param("ownerUserId") String ownerUserId,
                                             @Param("from") LocalDate from,
                                             @Param("to") LocalDate to);

    List<AmountByIdRecord> sumExpenseByCategoryLevel1(@Param("ownerUserId") String ownerUserId,
                                                      @Param("from") LocalDate from,
                                                      @Param("to") LocalDate to);

    List<AmountByIdRecord> sumExpenseByTag(@Param("ownerUserId") String ownerUserId,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);

    List<RecentTransactionRecord> selectRecentTransactions(@Param("ownerUserId") String ownerUserId,
                                                           @Param("from") LocalDate from,
                                                           @Param("to") LocalDate to,
                                                           @Param("limit") int limit);
}
