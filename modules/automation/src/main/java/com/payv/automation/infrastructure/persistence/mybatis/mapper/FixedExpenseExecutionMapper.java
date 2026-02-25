package com.payv.automation.infrastructure.persistence.mybatis.mapper;

import com.payv.automation.infrastructure.persistence.mybatis.record.FixedExpenseExecutionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * fixed_expense_execution 테이블용 MyBatis Mapper.
 */
@Mapper
public interface FixedExpenseExecutionMapper {

    /**
     * execution_id 기준 upsert.
     */
    int upsert(FixedExpenseExecutionRecord record);

    FixedExpenseExecutionRecord selectByIdAndOwner(@Param("executionId") String executionId,
                                                   @Param("ownerUserId") String ownerUserId);

    int countByDefinitionAndScheduledDate(@Param("ownerUserId") String ownerUserId,
                                          @Param("definitionId") String definitionId,
                                          @Param("scheduledDate") LocalDate scheduledDate);

    /**
     * 특정 날짜의 실행 예정 건 조회.
     */
    List<FixedExpenseExecutionRecord> selectPlannedByDate(@Param("scheduledDate") LocalDate scheduledDate);
}
