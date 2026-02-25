package com.payv.automation.infrastructure.persistence.mybatis.mapper;

import com.payv.automation.infrastructure.persistence.mybatis.record.FixedExpenseDefinitionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * fixed_expense_definition 테이블용 MyBatis Mapper.
 */
@Mapper
public interface FixedExpenseDefinitionMapper {

    /**
     * definition_id 기준 upsert.
     */
    int upsert(FixedExpenseDefinitionRecord record);

    FixedExpenseDefinitionRecord selectByIdAndOwner(@Param("definitionId") String definitionId,
                                                    @Param("ownerUserId") String ownerUserId);

    List<FixedExpenseDefinitionRecord> selectAllActiveByOwner(@Param("ownerUserId") String ownerUserId);

    List<FixedExpenseDefinitionRecord> selectAllActiveScheduledOn(@Param("runDay") int runDay,
                                                                  @Param("lastDayOfMonth") int lastDayOfMonth,
                                                                  @Param("runDateIsEom") boolean runDateIsEom);
}
