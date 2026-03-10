package com.payv.classification.application.query;

import com.payv.classification.application.query.model.CategoryTreeView;
import com.payv.classification.domain.model.Category;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.domain.repository.CategoryRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CategoryQueryServiceTest {

    private static final String OWNER = "user-1";

    private InMemoryCategoryRepository repository;
    private CategoryQueryService service;

    @Before
    public void setUp() {
        repository = new InMemoryCategoryRepository();
        service = new CategoryQueryService(repository);
    }

    @Test
    public void getAll_returnsTreeViews() {
        // Given
        Category food = CategoryTestDataBuilder.rootWithChild("Food", "Cafe");
        repository.save(food, OWNER);

        // When
        List<CategoryTreeView> views = service.getAll(OWNER);

        // Then
        assertEquals(1, views.size());
        assertEquals("Food", views.get(0).getName());
        assertEquals(1, views.get(0).getChildren().size());
        assertEquals("Cafe", views.get(0).getChildren().get(0).getName());
    }

    @Test
    public void getRoot_returnsOptionalView() {
        // Given
        Category root = CategoryTestDataBuilder.root("Life");
        repository.save(root, OWNER);

        // When
        Optional<CategoryTreeView> view = service.getRoot(root.getId(), OWNER);

        // Then
        assertTrue(view.isPresent());
        assertEquals(root.getId().getValue(), view.get().getCategoryId());
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
        public List<Category> findNamesByIds(String ownerUserId, Collection<CategoryId> categoryIds) {
            List<Category> result = new ArrayList<>();
            if (categoryIds == null || categoryIds.isEmpty()) return result;

            for (Category root : findAllCategory(ownerUserId)) {
                if (categoryIds.contains(root.getId())) {
                    result.add(root);
                }
                for (Category child : root.getChildren()) {
                    if (categoryIds.contains(child.getId())) {
                        result.add(child);
                    }
                }
            }
            return result;
        }
    }
}
