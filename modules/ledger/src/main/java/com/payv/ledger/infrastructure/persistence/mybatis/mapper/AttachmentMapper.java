package com.payv.ledger.infrastructure.persistence.mybatis.mapper;

import com.payv.ledger.infrastructure.persistence.mybatis.record.AttachmentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttachmentMapper {

    void deleteByTransactionId(@Param("transactionId") String transactionId);
    void insertAttachments(@Param("records") List<AttachmentRecord> records);
    List<AttachmentRecord> selectByTransactionId(@Param("transactionId") String transactionId);


}
