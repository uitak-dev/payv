package com.payv.ledger.application.command;

import com.payv.ledger.application.port.AttachmentStoragePort;
import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.repository.AttachmentRepository;
import com.payv.ledger.domain.repository.TransactionRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AttachmentCommandServiceTest {

    private static final String OWNER = "user-1";
    private static final TransactionId TRANSACTION_ID = TransactionId.of("tx-1");

    private InMemoryAttachmentRepository attachmentRepository;
    private FakeAttachmentStoragePort storagePort;
    private TransactionTemplate txTemplate;

    private AttachmentCommandService service;

    @Before
    public void setUp() {
        attachmentRepository = new InMemoryAttachmentRepository();
        storagePort = new FakeAttachmentStoragePort();
        txTemplate = mock(TransactionTemplate.class);

        when(txTemplate.execute(any(TransactionCallback.class))).thenAnswer((Answer<Object>) invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        service = new AttachmentCommandService(
                new NoOpTransactionRepository(),
                attachmentRepository,
                storagePort,
                txTemplate
        );
    }

    @After
    public void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void upload_rejectsOverLimit() {
        // Given
        attachmentRepository.activeCount = 2;

        // When
        service.upload(TRANSACTION_ID, OWNER, new StubMultipartFile("file.txt", "text/plain", 10));
    }

    @Test
    public void upload_registersCommitHookAndMarksStored() {
        // Given
        TransactionSynchronizationManager.initSynchronization();

        // When
        AttachmentId id = service.upload(TRANSACTION_ID, OWNER, new StubMultipartFile("a.txt", "text/plain", 12));

        // Then (pre-commit)
        assertNotNull(attachmentRepository.lastInserted);
        assertEquals(id, attachmentRepository.lastInserted.getId());
        assertEquals(Attachment.Status.UPLOADING, attachmentRepository.lastInserted.getStatus());
        assertTrue(storagePort.savedToStaging);
        assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());

        // Simulate commit
        for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
            sync.afterCommit();
            sync.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        }

        assertTrue(storagePort.movedToFinal);
        assertEquals(id, attachmentRepository.markStoredId);
    }

    @Test
    public void upload_marksFailedWhenFinalizeThrows() {
        // Given
        storagePort.throwOnMove = true;
        TransactionSynchronizationManager.initSynchronization();

        // When
        AttachmentId id = service.upload(TRANSACTION_ID, OWNER, new StubMultipartFile("b.txt", "text/plain", 20));
        for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
            sync.afterCommit();
            sync.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        }

        // Then
        assertEquals(id, attachmentRepository.markFailedId);
        assertNotNull(attachmentRepository.markFailedReason);
        assertFalse(attachmentRepository.markFailedReason.trim().isEmpty());
    }

    @Test
    public void upload_deletesStagingOnRollback() {
        // Given
        TransactionSynchronizationManager.initSynchronization();

        // When
        service.upload(TRANSACTION_ID, OWNER, new StubMultipartFile("c.txt", "text/plain", 30));
        for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
            sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        }

        // Then
        assertTrue(storagePort.deletedStaging);
    }

    private static class InMemoryAttachmentRepository implements AttachmentRepository {
        int activeCount = 0;

        Attachment lastInserted;
        AttachmentId markStoredId;
        AttachmentId markFailedId;
        String markFailedReason;
        Attachment storedAttachment;

        @Override
        public int countActiveByTransactionId(TransactionId id, String ownerUserId) {
            return activeCount;
        }

        @Override
        public void insertUploading(Attachment attachment) {
            this.lastInserted = attachment;
        }

        @Override
        public List<Attachment> findStoredByTransactionId(TransactionId id, String ownerUserId) {
            return Collections.emptyList();
        }

        @Override
        public Optional<Attachment> findById(AttachmentId id, String ownerUserId) {
            return Optional.ofNullable(storedAttachment);
        }

        @Override
        public void deleteById(AttachmentId id, String ownerUserId) {
        }

        @Override
        public void markStored(AttachmentId id, String ownerUserId) {
            this.markStoredId = id;
        }

        @Override
        public void markFailed(AttachmentId id, String ownerUserId, String failureReason) {
            this.markFailedId = id;
            this.markFailedReason = failureReason;
        }
    }

    private static class FakeAttachmentStoragePort implements AttachmentStoragePort {
        boolean savedToStaging = false;
        boolean movedToFinal = false;
        boolean deletedStaging = false;
        boolean throwOnMove = false;

        @Override
        public StoragePlan plan(String ownerUserId, TransactionId transactionId, AttachmentId attachmentId,
                                String uploadFileName, String contentType) {
            return new StoragePlan(
                    uploadFileName, contentType, 0,
                    "/final", "stored-" + attachmentId.getValue(),
                    "/staging", "staging-" + attachmentId.getValue()
            );
        }

        @Override
        public void saveToStaging(StoragePlan plan, MultipartFile file) {
            this.savedToStaging = true;
        }

        @Override
        public void moveStagingToFinal(StoragePlan plan) {
            if (throwOnMove) {
                throw new RuntimeException("move failed");
            }
            this.movedToFinal = true;
        }

        @Override
        public void deleteStagingQuietly(StoragePlan plan) {
            this.deletedStaging = true;
        }

        @Override
        public void deleteFinalQuietly(StoragePlan plan) {
            this.movedToFinal = true;
        }
    }

    private static class NoOpTransactionRepository implements TransactionRepository {
        @Override
        public void save(Transaction transaction) {
        }

        @Override
        public Optional<Transaction> findById(TransactionId id, String ownerUserId) {
            return Optional.empty();
        }

        @Override
        public void deleteById(TransactionId id, String ownerUserId) {
        }
    }

    private static class StubMultipartFile implements MultipartFile {
        private final String name;
        private final String contentType;
        private final byte[] bytes;

        private StubMultipartFile(String name, String contentType, int size) {
            this.name = name;
            this.contentType = contentType;
            this.bytes = new byte[size];
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return bytes.length == 0;
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public byte[] getBytes() {
            return bytes;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            try (OutputStream out = new FileOutputStream(dest)) {
                out.write(bytes);
            }
        }
    }
}
