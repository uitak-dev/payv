package com.payv.asset.infrastructure.persistence.mybatis.mapper;

import com.payv.asset.infrastructure.persistence.mybatis.record.AssetRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssetMapper {

    int upsert(AssetRecord record);

    AssetRecord selectByIdAndOwner(@Param("assetId") String assetId,
                                   @Param("ownerUserId") String ownerUserId);

    List<AssetRecord> selectAllByOwner(@Param("ownerUserId") String ownerUserId);

    List<AssetRecord> selectNamesByIds(@Param("ownerUserId") String ownerUserId,
                                       @Param("ids") List<String> ids);
}
