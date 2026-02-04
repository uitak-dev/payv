package com.payv.classification.infrastructure.persistence.mybatis.mapper;

import com.payv.classification.infrastructure.persistence.mybatis.record.CategoryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /** ---- Command ---- */
    int upsert(CategoryRecord record);
    int deleteParentAndChildrenById(@Param("categoryId") String categoryId);
    int deleteByIds(@Param("ids") List<String> ids);


    /** ---- Query ---- */
    CategoryRecord selectRootById(@Param("categoryId") String categoryId,
                                    @Param("ownerUserId") String ownerUserId);

    List<CategoryRecord> selectRootsByOwner(@Param("ownerUserId") String ownerUserId);

    List<CategoryRecord> selectChildrenByOwner(@Param("parentId") String parentId,
                                               @Param("ownerUserId") String ownerUserId);

    int countRootsByOwner(@Param("ownerUserId") String ownerUserId);

    int countChildrenByOwner(@Param("parentId") String parentId,
                             @Param("ownerUserId") String ownerUserId);
}
