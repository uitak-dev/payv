package com.payv.ledger.infrastructure.persistence.mybatis;

import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.Money;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.model.TransactionType;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.AttachmentMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransactionMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransactionTagMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionTagRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MyBatisTransactionRepositoryTest {

    private TransactionMapper transactionMapper;
    private TransactionTagMapper transactionTagMapper;
    private AttachmentMapper attachmentMapper;
    private MyBatisTransactionRepository repository;

    @Before
    public void setUp() {
        transactionMapper = mock(TransactionMapper.class);
        transactionTagMapper = mock(TransactionTagMapper.class);
        attachmentMapper = mock(AttachmentMapper.class);
        repository = new MyBatisTransactionRepository(transactionMapper, transactionTagMapper, attachmentMapper);
    }

    @Test
    public void save_appliesTagDiff() {
        // given
        Transaction tx = Transaction.createManual(
                "user-1",
                TransactionType.EXPENSE,
                Money.generate(1000L),
                LocalDate.of(2026, 2, 6),
                "asset-1",
                "cat-1"
        );
        tx.updateTags(new java.util.LinkedHashSet<String>(Arrays.asList("tag-keep", "tag-new")));

        when(transactionTagMapper.selectByTransactionId(tx.getId().getValue())).thenReturn(Arrays.asList(
                new TransactionTagRecord(tx.getId().getValue(), "tag-old"),
                new TransactionTagRecord(tx.getId().getValue(), "tag-keep")
        ));

        // when
        repository.save(tx);

        // then
        verify(transactionMapper).upsert(any(TransactionRecord.class));

        ArgumentCaptor<List<String>> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(transactionTagMapper).deleteByTransactionIdAndTagIds(eq(tx.getId().getValue()), deleteCaptor.capture());
        assertEquals(1, deleteCaptor.getValue().size());
        assertEquals("tag-old", deleteCaptor.getValue().get(0));

        ArgumentCaptor<List<TransactionTagRecord>> insertCaptor = ArgumentCaptor.forClass(List.class);
        verify(transactionTagMapper).insertTags(insertCaptor.capture());
        assertEquals(1, insertCaptor.getValue().size());
        assertEquals("tag-new", insertCaptor.getValue().get(0).getTagId());
        assertEquals("user-1", insertCaptor.getValue().get(0).getOwnerUserId());
    }

    @Test
    public void save_doesNotTouchAttachments() {
        // given
        Transaction tx = Transaction.createManual(
                "user-1",
                TransactionType.EXPENSE,
                Money.generate(2000L),
                LocalDate.of(2026, 2, 6),
                "asset-1",
                "cat-1"
        );
        String txId = tx.getId().getValue();
        String owner = tx.getOwnerUserId();

        tx.updateTags(new LinkedHashSet<String>(Collections.singletonList("tag-1")));
        tx.updateAttachments(Collections.singletonList(
                Attachment.of(
                        AttachmentId.of("att-1"),
                        TransactionId.of(txId),
                        owner,
                        "a.png",
                        "a-stored.png",
                        "/final",
                        "/staging",
                        "a.upload",
                        "image/png",
                        10L,
                        Attachment.Status.STORED,
                        null
                )
        ));

        when(transactionTagMapper.selectByTransactionId(txId)).thenReturn(Collections.emptyList());

        // when
        repository.save(tx);

        // then
        verify(transactionTagMapper).insertTags(anyList());
        verifyNoInteractions(attachmentMapper);
        verify(transactionTagMapper, never()).deleteByTransactionId(anyString());
    }

    @Test
    public void save_doesNothingWhenTagNoDiff() {
        // given
        Transaction tx = Transaction.createManual(
                "user-1",
                TransactionType.EXPENSE,
                Money.generate(1500L),
                LocalDate.of(2026, 2, 6),
                "asset-1",
                "cat-1"
        );
        String txId = tx.getId().getValue();

        tx.updateTags(new LinkedHashSet<String>(Collections.singletonList("tag-1")));

        when(transactionTagMapper.selectByTransactionId(txId)).thenReturn(Collections.singletonList(
                new TransactionTagRecord(txId, "tag-1")
        ));

        // when
        repository.save(tx);

        // then
        verify(transactionTagMapper, never()).deleteByTransactionIdAndTagIds(anyString(), anyList());
        verify(transactionTagMapper, never()).insertTags(anyList());
        verifyNoInteractions(attachmentMapper);
        assertTrue(true);
    }
}
