package com.payv.classification.infrastructure.persistence.mybatis.mapper;

import com.payv.classification.infrastructure.persistence.mybatis.record.TagRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {

    /** ---- Command ---- */
    int upsert(TagRecord record);

    int deleteById(@Param("tagId") String tagId);


    /** ---- Query ---- */
    TagRecord selectByIdAndOwner(@Param("tagId") String tagId,
                                 @Param("ownerUserId") String ownerUserId);

    List<TagRecord> selectAllByOwner(@Param("ownerUserId") String ownerUserId);

    int countByOwner(@Param("ownerUserId") String ownerUserId);
}
