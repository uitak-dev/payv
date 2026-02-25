package com.payv.automation.application.command;

import com.payv.automation.application.command.model.CreateFixedExpenseCommand;
import com.payv.automation.application.command.model.DeactivateFixedExpenseCommand;
import com.payv.automation.application.command.model.UpdateFixedExpenseCommand;
import com.payv.automation.application.exception.FixedExpenseNotFoundException;
import com.payv.automation.application.port.AssetValidationPort;
import com.payv.automation.application.port.ClassificationValidationPort;
import com.payv.automation.domain.model.FixedExpenseDefinition;
import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import com.payv.automation.domain.repository.FixedExpenseDefinitionRepository;
import com.payv.common.error.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FixedExpenseCommandService {

    private final FixedExpenseDefinitionRepository definitionRepository;
    private final AssetValidationPort assetValidationPort;
    private final ClassificationValidationPort classificationValidationPort;

    public FixedExpenseDefinitionId create(CreateFixedExpenseCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        assetValidationPort.validateAssetIds(Collections.singleton(command.getAssetId()), ownerUserId);
        classificationValidationPort.validateCategorization(
                toCategoryIds(command.getCategoryIdLevel1(), command.getCategoryIdLevel2()),
                ownerUserId
        );

        FixedExpenseDefinition definition = FixedExpenseDefinition.create(
                ownerUserId,
                command.getName(),
                command.getAmount(),
                command.getAssetId(),
                command.getCategoryIdLevel1(),
                command.getCategoryIdLevel2(),
                command.getMemo(),
                command.getDayOfMonth(),
                command.isEndOfMonth()
        );
        definitionRepository.save(definition);
        return definition.getId();
    }

    public void update(UpdateFixedExpenseCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        FixedExpenseDefinition definition = definitionRepository.findById(command.getDefinitionId(), ownerUserId)
                .filter(FixedExpenseDefinition::isActive)
                .orElseThrow(FixedExpenseNotFoundException::new);

        assetValidationPort.validateAssetIds(Collections.singleton(command.getAssetId()), ownerUserId);
        classificationValidationPort.validateCategorization(
                toCategoryIds(command.getCategoryIdLevel1(), command.getCategoryIdLevel2()),
                ownerUserId
        );

        definition.update(
                command.getName(),
                command.getAmount(),
                command.getAssetId(),
                command.getCategoryIdLevel1(),
                command.getCategoryIdLevel2(),
                command.getMemo(),
                command.getDayOfMonth(),
                command.isEndOfMonth()
        );
        definitionRepository.save(definition);
    }

    public void deactivate(DeactivateFixedExpenseCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        FixedExpenseDefinition definition = definitionRepository.findById(command.getDefinitionId(), ownerUserId)
                .filter(FixedExpenseDefinition::isActive)
                .orElseThrow(FixedExpenseNotFoundException::new);

        definition.deactivate();
        definitionRepository.save(definition);
    }

    private List<String> toCategoryIds(String categoryIdLevel1, String categoryIdLevel2) {
        List<String> ids = new ArrayList<>();
        ids.add(categoryIdLevel1);
        if (categoryIdLevel2 != null && !categoryIdLevel2.trim().isEmpty()) {
            ids.add(categoryIdLevel2);
        }
        return ids;
    }

    private static void requireOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            throw new InvalidRequestException("ownerUserId must not be blank");
        }
    }
}
