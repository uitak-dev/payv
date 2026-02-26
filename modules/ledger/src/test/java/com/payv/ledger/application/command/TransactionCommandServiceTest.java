package com.payv.ledger.application.command;

import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.port.AssetValidationPort;
import com.payv.ledger.application.port.AttachmentStoragePort;
import com.payv.ledger.application.port.ClassificationValidationPort;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.Money;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.model.TransactionType;
import com.payv.ledger.domain.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.*;

public class TransactionCommandServiceTest {

    private static final String OWNER = "user-1";

    private InMemoryTransactionRepository repository;
    private TransactionCommandService service;

    @Before
    public void setUp() {
        repository = new InMemoryTransactionRepository();
        service = new TransactionCommandService(
                repository,
                new NoOpClassificationValidationPort(),
                new NoOpAssetValidationPort(),
                new NoOpAttachmentStoragePort(),
                event -> { }
        );
    }

    @Test
    public void createManual_savesTransactionWithOptionalFields() {
        // Given
        CreateTransactionCommand command = TransactionTestDataBuilder
                .manual("asset-1", "cat-1")
                .memo("note")
                .categoryIdLevel2("cat-1-1")
                .tag("tag-1")
                .tag("tag-2")
                .build();

        // When
        TransactionId id = service.createManual(command, OWNER);

        // Then
        Transaction saved = repository.findById(id, OWNER).orElse(null);
        assertNotNull(saved);
        assertEquals(OWNER, saved.getOwnerUserId());
        assertEquals("asset-1", saved.getAssetId());
        assertEquals("cat-1", saved.getCategoryIdLevel1());
        assertEquals("cat-1-1", saved.getCategoryIdLevel2());
        assertEquals("note", saved.getMemo());
        assertEquals(2, saved.getTagIds().size());
        assertTrue(saved.getTagIds().contains("tag-1"));
        assertTrue(saved.getTagIds().contains("tag-2"));
    }

    @Test
    public void createManual_allowsEmptyOptionalFields() {
        // Given
        CreateTransactionCommand command = TransactionTestDataBuilder
                .manual("asset-2", "cat-2")
                .build();

        // When
        TransactionId id = service.createManual(command, OWNER);

        // Then
        Transaction saved = repository.findById(id, OWNER).orElse(null);
        assertNotNull(saved);
        assertNull(saved.getCategoryIdLevel2());
        assertNull(saved.getMemo());
        assertTrue(saved.getTagIds().isEmpty());
    }

    @Test
    public void deleteTransaction_removesOwnedTransaction() {
        // Given
        CreateTransactionCommand command = TransactionTestDataBuilder
                .manual("asset-3", "cat-3")
                .build();
        TransactionId id = service.createManual(command, OWNER);

        // When
        service.deleteTransaction(id, OWNER);

        // Then
        assertFalse(repository.findById(id, OWNER).isPresent());
    }

    private static class TransactionTestDataBuilder {
        private final CreateTransactionCommand command;

        private TransactionTestDataBuilder(String assetId, String categoryIdLevel1) {
            this.command = CreateTransactionCommand.builder()
                    .transactionType(TransactionType.EXPENSE)
                    .amount(Money.generate(1200L))
                    .transactionDate(LocalDate.of(2026, 2, 5))
                    .assetId(assetId)
                    .categoryIdLevel1(categoryIdLevel1)
                    .build();
        }

        static TransactionTestDataBuilder manual(String assetId, String categoryIdLevel1) {
            return new TransactionTestDataBuilder(assetId, categoryIdLevel1);
        }

        TransactionTestDataBuilder memo(String memo) {
            command.setMemo(memo);
            return this;
        }

        TransactionTestDataBuilder categoryIdLevel2(String categoryIdLevel2) {
            command.setCategoryIdLevel2(categoryIdLevel2);
            return this;
        }

        TransactionTestDataBuilder tag(String tagId) {
            command.addTagId(tagId);
            return this;
        }

        CreateTransactionCommand build() {
            return command;
        }
    }

    private static class InMemoryTransactionRepository implements TransactionRepository {
        private final Map<String, Map<TransactionId, Transaction>> storeByOwner = new HashMap<>();

        @Override
        public void save(Transaction transaction) {
            storeByOwner
                    .computeIfAbsent(transaction.getOwnerUserId(), k -> new LinkedHashMap<>())
                    .put(transaction.getId(), transaction);
        }

        @Override
        public Optional<Transaction> findById(TransactionId id, String ownerUserId) {
            Map<TransactionId, Transaction> ownerStore = storeByOwner.get(ownerUserId);
            if (ownerStore == null) return Optional.empty();
            return Optional.ofNullable(ownerStore.get(id));
        }

        @Override
        public void deleteById(TransactionId id, String ownerUserId) {
            Map<TransactionId, Transaction> ownerStore = storeByOwner.get(ownerUserId);
            if (ownerStore == null) return;
            ownerStore.remove(id);
        }
    }

    private static class NoOpClassificationValidationPort implements ClassificationValidationPort {
        @Override
        public void validateTagIds(Collection<String> tagIds, String ownerUserId) {
        }

        @Override
        public void validateCategorization(Collection<String> categoryIds, String ownerUserId) {
        }
    }

    private static class NoOpAssetValidationPort implements AssetValidationPort {
        @Override
        public void validateAssertId(String assetId, String ownerUserId) {
        }
    }

    private static class NoOpAttachmentStoragePort implements AttachmentStoragePort {
        @Override
        public StoragePlan plan(String ownerUserId, TransactionId transactionId, AttachmentId attachmentId, String uploadFileName, String contentType) {
            return null;
        }

        @Override
        public void saveToStaging(StoragePlan plan, org.springframework.web.multipart.MultipartFile file) {
        }

        @Override
        public void moveStagingToFinal(StoragePlan plan) {
        }

        @Override
        public void deleteStagingQuietly(StoragePlan plan) {
        }

        @Override
        public void deleteFinalQuietly(StoragePlan plan) {
        }

        @Override
        public byte[] readFinal(String storagePath, String storedFileName) {
            return new byte[0];
        }
    }
}
