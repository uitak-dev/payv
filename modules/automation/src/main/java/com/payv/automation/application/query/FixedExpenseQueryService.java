package com.payv.automation.application.query;

import com.payv.automation.application.port.AssetQueryPort;
import com.payv.automation.application.port.ClassificationQueryPort;
import com.payv.contracts.classification.dto.CategoryTreePublicDto;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.automation.application.query.model.FixedExpenseView;
import com.payv.automation.domain.model.FixedExpenseDefinition;
import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import com.payv.automation.domain.repository.FixedExpenseDefinitionRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * Automation BC의 고정비 조회 서비스.
 * - 고정비 정의 목록/상세 조회와 폼 옵션(자산/카테고리) 조회를 제공한다.
 * - 화면에서 필요한 표준 뷰 모델을 미리 조합해, presentation 계층의 데이터 결합 부담을 줄인다.
 */
public class FixedExpenseQueryService {

    private final FixedExpenseDefinitionRepository definitionRepository;
    private final AssetQueryPort assetQueryPort;
    private final ClassificationQueryPort classificationQueryPort;

    /**
     * 소유자의 전체 활성 고정비 정의를 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @return 고정비 뷰 목록
     */
    public List<FixedExpenseView> getAll(String ownerUserId) {
        List<FixedExpenseDefinition> definitions = definitionRepository.findAllActiveByOwner(ownerUserId);
        if (definitions.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> assetIds = definitions.stream()
                .map(FixedExpenseDefinition::getAssetId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> categoryIds = new LinkedHashSet<>();
        for (FixedExpenseDefinition definition : definitions) {
            categoryIds.add(definition.getCategoryIdLevel1());
            if (definition.getCategoryIdLevel2() != null) {
                categoryIds.add(definition.getCategoryIdLevel2());
            }
        }

        Map<String, String> assetNames = toNameMap(assetQueryPort.getAssetNames(assetIds, ownerUserId));
        Map<String, String> categoryNames = toNameMap(classificationQueryPort.getCategoryNames(categoryIds, ownerUserId));

        List<FixedExpenseView> result = new ArrayList<>(definitions.size());
        for (FixedExpenseDefinition definition : definitions) {
            result.add(toView(definition, assetNames, categoryNames));
        }
        return result;
    }

    /**
     * 활성 고정비 정의 단건을 조회한다.
     *
     * @param definitionId 고정비 정의 ID
     * @param ownerUserId 소유 사용자 ID
     * @return 고정비 뷰. 없거나 비활성이면 {@link Optional#empty()}
     */
    public Optional<FixedExpenseView> get(FixedExpenseDefinitionId definitionId, String ownerUserId) {
        Optional<FixedExpenseDefinition> found = definitionRepository.findById(definitionId, ownerUserId)
                .filter(FixedExpenseDefinition::isActive);
        if (!found.isPresent()) {
            return Optional.empty();
        }

        FixedExpenseDefinition definition = found.get();
        Map<String, String> assetNames = toNameMap(assetQueryPort.getAssetNames(
                Collections.singleton(definition.getAssetId()),
                ownerUserId
        ));

        Set<String> categoryIds = new LinkedHashSet<>();
        categoryIds.add(definition.getCategoryIdLevel1());
        if (definition.getCategoryIdLevel2() != null) {
            categoryIds.add(definition.getCategoryIdLevel2());
        }
        Map<String, String> categoryNames = toNameMap(classificationQueryPort.getCategoryNames(categoryIds, ownerUserId));

        return Optional.of(toView(definition, assetNames, categoryNames));
    }

    /**
     * 고정비 폼에서 사용할 자산 선택 옵션을 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @return 자산 옵션 목록
     */
    public List<IdNamePublicDto> getAssetOptions(String ownerUserId) {
        return assetQueryPort.getAllAssets(ownerUserId);
    }

    /**
     * 고정비 폼에서 사용할 카테고리 선택 옵션을 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @return 카테고리 트리 옵션 목록
     */
    public List<CategoryTreePublicDto> getCategoryOptions(String ownerUserId) {
        return classificationQueryPort.getAllCategories(ownerUserId);
    }

    private FixedExpenseView toView(FixedExpenseDefinition definition,
                                    Map<String, String> assetNames,
                                    Map<String, String> categoryNames) {
        String level1Id = definition.getCategoryIdLevel1();
        String level2Id = definition.getCategoryIdLevel2();
        return new FixedExpenseView(
                definition.getId().getValue(),
                definition.getName(),
                definition.getAmount(),
                definition.getAssetId(),
                assetNames.get(definition.getAssetId()),
                level1Id,
                categoryNames.get(level1Id),
                level2Id,
                level2Id == null ? null : categoryNames.get(level2Id),
                definition.getMemo(),
                definition.getDayOfMonth(),
                definition.isEndOfMonth()
        );
    }

    private Map<String, String> toNameMap(List<IdNamePublicDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (IdNamePublicDto row : rows) {
            result.put(row.getId(), row.getName());
        }
        return result;
    }
}
