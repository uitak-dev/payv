package com.payv.budget.infrastructure.persistence.mybatis.mapper;

import com.payv.budget.infrastructure.persistence.mybatis.record.BudgetRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BudgetMapper {

    int upsert(BudgetRecord record);

    BudgetRecord selectByIdAndOwner(@Param("budgetId") String budgetId,
                                    @Param("ownerUserId") String ownerUserId);

    List<BudgetRecord> selectAllByOwnerAndMonth(@Param("ownerUserId") String ownerUserId,
                                                @Param("targetMonth") String targetMonth);

    int deactivateCategoryBudgetsNotIn(@Param("ownerUserId") String ownerUserId,
                                       @Param("activeCategoryIds") List<String> activeCategoryIds);

    int deactivateAllCategoryBudgets(@Param("ownerUserId") String ownerUserId);
}
