package com.payv.ledger.infrastructure.persistence.mybatis.mapper;

import com.payv.ledger.infrastructure.persistence.mybatis.record.AttachmentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttachmentMapper {

    int countActiveByTransactionId(@Param("transactionId") String transactionId,
                                   @Param("ownerUserId") String ownerUserId);
    void deleteByTransactionId(@Param("transactionId") String transactionId);
    void insertUploading(AttachmentRecord record);
    void insertAttachments(@Param("records") List<AttachmentRecord> records);
    List<AttachmentRecord> selectStoredByTransactionId(@Param("transactionId") String transactionId,
                                                       @Param("ownerUserId") String ownerUserId);

    void markStored(@Param("attachmentId") String attachmentId,
                    @Param("ownerUserId") String ownerUserId);

    void markFailed(@Param("attachmentId") String attachmentId,
                    @Param("ownerUserId") String ownerUserId,
                    @Param("failureReason") String failureReason);
}
