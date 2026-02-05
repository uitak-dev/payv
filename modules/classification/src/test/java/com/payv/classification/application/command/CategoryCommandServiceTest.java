package com.payv.classification.application.command;

import com.payv.classification.application.command.model.CreateChildCategoryCommand;
import com.payv.classification.application.command.model.CreateParentCategoryCommand;
import com.payv.classification.application.command.model.DeactivateChildCategoryCommand;
import com.payv.classification.application.command.model.DeactivateRootCategoryCommand;
import com.payv.classification.application.command.model.RenameChildCategoryCommand;
import com.payv.classification.application.command.model.RenameRootCategoryCommand;
import com.payv.classification.domain.model.Category;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.domain.repository.CategoryRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CategoryCommandServiceTest {

    private static final String OWNER = "user-1";

    private InMemoryCategoryRepository repository;
    private CategoryCommandService service;

    @Before
    public void setUp() {
        repository = new InMemoryCategoryRepository();
        service = new CategoryCommandService(repository);
    }

    @Test
    public void createParent_createsRootCategory() {
        // Given
        CreateParentCategoryCommand command = new CreateParentCategoryCommand("Food");

        // When
        CategoryId id = service.createParent(command, OWNER);

        // Then
        Category root = repository.findRootById(id, OWNER).orElse(null);
        assertNotNull(root);
        assertEquals("Food", root.getName());
        assertTrue(root.isRoot());
    }

    @Test(expected = IllegalStateException.class)
    public void createParent_rejectsDuplicateName() {
        // Given
        repository.save(CategoryTestDataBuilder.root("Food"), OWNER);

        // When
        service.createParent(new CreateParentCategoryCommand("Food"), OWNER);
    }

    @Test
    public void createChild_addsChildUnderRoot() {
        // Given
        Category root = CategoryTestDataBuilder.root("Transport");
        repository.save(root, OWNER);

        // When
        CategoryId childId = service.createChild(
                new CreateChildCategoryCommand(root.getId(), "Taxi"),
                OWNER
        );

        // Then
        Category savedRoot = repository.findRootById(root.getId(), OWNER).orElse(null);
        assertNotNull(savedRoot);
        assertEquals(1, savedRoot.getChildren().size());
        assertEquals(childId, savedRoot.getChildren().get(0).getId());
    }

    @Test
    public void renameRoot_updatesName() {
        // Given
        Category root = CategoryTestDataBuilder.root("Meals");
        repository.save(root, OWNER);

        // When
        service.renameRoot(new RenameRootCategoryCommand(root.getId(), "Food"), OWNER);

        // Then
        Category saved = repository.findRootById(root.getId(), OWNER).orElse(null);
        assertNotNull(saved);
        assertEquals("Food", saved.getName());
    }

    @Test(expected = IllegalStateException.class)
    public void renameRoot_rejectsDuplicateName() {
        // Given
        Category rootA = CategoryTestDataBuilder.root("Food");
        Category rootB = CategoryTestDataBuilder.root("Travel");
        repository.save(rootA, OWNER);
        repository.save(rootB, OWNER);

        // When
        service.renameRoot(new RenameRootCategoryCommand(rootB.getId(), "Food"), OWNER);
    }

    @Test
    public void renameChild_updatesName() {
        // Given
        Category root = CategoryTestDataBuilder.rootWithChild("Home", "Rent");
        repository.save(root, OWNER);
        CategoryId childId = root.getChildren().get(0).getId();

        // When
        service.renameChild(new RenameChildCategoryCommand(root.getId(), childId, "Mortgage"), OWNER);

        // Then
        Category saved = repository.findRootById(root.getId(), OWNER).orElse(null);
        assertNotNull(saved);
        assertEquals("Mortgage", saved.getChildren().get(0).getName());
    }

    @Test
    public void deactivateRoot_deactivatesRootAndChildren() {
        // Given
        Category root = CategoryTestDataBuilder.rootWithChild("Life", "Health");
        repository.save(root, OWNER);
        CategoryId childId = root.getChildren().get(0).getId();

        // When
        service.deactivateRoot(new DeactivateRootCategoryCommand(root.getId()), OWNER);

        // Then
        Category saved = repository.findRootById(root.getId(), OWNER).orElse(null);
        assertNotNull(saved);
        assertFalse(saved.isActive());
        assertFalse(saved.getChildren().get(0).isActive());
        assertEquals(childId, saved.getChildren().get(0).getId());
    }

    @Test
    public void deactivateChild_deactivatesOnlyChild() {
        // Given
        Category root = CategoryTestDataBuilder.rootWithChild("Leisure", "Movies");
        repository.save(root, OWNER);
        CategoryId childId = root.getChildren().get(0).getId();

        // When
        service.deactivateChild(new DeactivateChildCategoryCommand(root.getId(), childId), OWNER);

        // Then
        Category saved = repository.findRootById(root.getId(), OWNER).orElse(null);
        assertNotNull(saved);
        assertTrue(saved.isActive());
        assertFalse(saved.getChildren().get(0).isActive());
    }

    private static class CategoryTestDataBuilder {
        static Category root(String name) {
            return Category.createParent(OWNER, name);
        }

        static Category rootWithChild(String rootName, String childName) {
            Category root = Category.createParent(OWNER, rootName);
            root.createChild(childName, OWNER);
            return root;
        }
    }

    private static class InMemoryCategoryRepository implements CategoryRepository {
        private final Map<String, Map<CategoryId, Category>> storeByOwner = new HashMap<>();

        @Override
        public void save(Category rootCategory, String ownerUserId) {
            storeByOwner
                    .computeIfAbsent(ownerUserId, k -> new LinkedHashMap<>())
                    .put(rootCategory.getId(), rootCategory);
        }

        @Override
        public Optional<Category> findRootById(CategoryId parentId, String ownerUserId) {
            Map<CategoryId, Category> roots = storeByOwner.get(ownerUserId);
            if (roots == null) return Optional.empty();
            return Optional.ofNullable(roots.get(parentId));
        }

        @Override
        public List<Category> findAllCategory(String ownerUserId) {
            Map<CategoryId, Category> roots = storeByOwner.get(ownerUserId);
            if (roots == null) return Collections.emptyList();
            return new ArrayList<>(roots.values());
        }

        @Override
        public List<Category> findAllParentByOwner(String ownerUserId) {
            return findAllCategory(ownerUserId);
        }

        @Override
        public int countParents(String ownerUserId) {
            Map<CategoryId, Category> roots = storeByOwner.get(ownerUserId);
            return roots == null ? 0 : roots.size();
        }

        @Override
        public int countChildren(CategoryId parentId, String ownerUserId) {
            return findRootById(parentId, ownerUserId)
                    .map(root -> root.getChildren().size())
                    .orElse(0);
        }

        @Override
        public Map<CategoryId, String> findNamesByIds(String ownerUserId, Collection<CategoryId> categoryIds) {
            Map<CategoryId, String> result = new HashMap<>();
            if (categoryIds == null || categoryIds.isEmpty()) return result;

            for (Category root : findAllCategory(ownerUserId)) {
                if (categoryIds.contains(root.getId())) {
                    result.put(root.getId(), root.getName());
                }
                for (Category child : root.getChildren()) {
                    if (categoryIds.contains(child.getId())) {
                        result.put(child.getId(), child.getName());
                    }
                }
            }
            return result;
        }
    }
}
