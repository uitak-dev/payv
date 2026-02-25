package com.payv.automation.application.query;

import com.payv.automation.application.port.AssetQueryPort;
import com.payv.automation.application.port.ClassificationQueryPort;
import com.payv.automation.application.port.dto.AssetOptionDto;
import com.payv.automation.application.port.dto.CategoryTreeOptionDto;
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
public class FixedExpenseQueryService {

    private final FixedExpenseDefinitionRepository definitionRepository;
    private final AssetQueryPort assetQueryPort;
    private final ClassificationQueryPort classificationQueryPort;

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

        Map<String, String> assetNames = assetQueryPort.getAssetNames(assetIds, ownerUserId);
        Map<String, String> categoryNames = classificationQueryPort.getCategoryNames(categoryIds, ownerUserId);

        List<FixedExpenseView> result = new ArrayList<>(definitions.size());
        for (FixedExpenseDefinition definition : definitions) {
            result.add(toView(definition, assetNames, categoryNames));
        }
        return result;
    }

    public Optional<FixedExpenseView> get(FixedExpenseDefinitionId definitionId, String ownerUserId) {
        Optional<FixedExpenseDefinition> found = definitionRepository.findById(definitionId, ownerUserId)
                .filter(FixedExpenseDefinition::isActive);
        if (!found.isPresent()) {
            return Optional.empty();
        }

        FixedExpenseDefinition definition = found.get();
        Map<String, String> assetNames = assetQueryPort.getAssetNames(
                Collections.singleton(definition.getAssetId()),
                ownerUserId
        );

        Set<String> categoryIds = new LinkedHashSet<>();
        categoryIds.add(definition.getCategoryIdLevel1());
        if (definition.getCategoryIdLevel2() != null) {
            categoryIds.add(definition.getCategoryIdLevel2());
        }
        Map<String, String> categoryNames = classificationQueryPort.getCategoryNames(categoryIds, ownerUserId);

        return Optional.of(toView(definition, assetNames, categoryNames));
    }

    public List<AssetOptionDto> getAssetOptions(String ownerUserId) {
        return assetQueryPort.getAllAssets(ownerUserId);
    }

    public List<CategoryTreeOptionDto> getCategoryOptions(String ownerUserId) {
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
}
