package com.payv.ledger.infrastructure.persistence.mybatis.assembler;

import com.payv.ledger.domain.model.*;
import com.payv.ledger.infrastructure.persistence.mybatis.record.AttachmentRecord;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionTagRecord;

import java.util.List;
import java.util.stream.Collectors;

public final class TransactionAssembler {

    public static Transaction toEntity(TransactionRecord r,
                                       List<TransactionTagRecord> tagRecords,
                                       List<AttachmentRecord> attachmentRecords) {

        List<String> tagIds = tagRecords.stream()
                .map(TransactionTagRecord::getTagId)
                .collect(Collectors.toList());

        List<Attachment> attachments = attachmentRecords.stream()
                .map(AttachmentRecord::toEntity)
                .collect(Collectors.toList());

        return r.toEntity(tagIds, attachments);
    }
}
