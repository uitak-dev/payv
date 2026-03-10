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
/**
 * 고정비 마스터(FixedExpenseDefinition) 명령 서비스.
 * - 고정비 정의 생성/수정/비활성화를 처리한다.
 * - 자산/카테고리 유효성 검증을 선행하여, 이후 배치 실행 시 실패를 사전에 줄인다.
 */
public class FixedExpenseCommandService {

    private final FixedExpenseDefinitionRepository definitionRepository;
    private final AssetValidationPort assetValidationPort;
    private final ClassificationValidationPort classificationValidationPort;

    /**
     * 고정비 마스터를 생성한다.
     *
     * @param command 생성 요청(이름, 금액, 자산, 분류, 실행일)
     * @param ownerUserId 소유 사용자 ID
     * @return 생성된 고정비 정의 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     */
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

    /**
     * 고정비 마스터를 수정한다.
     *
     * @param command 수정 요청(정의 ID, 이름, 금액, 자산, 분류, 실행일)
     * @param ownerUserId 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws FixedExpenseNotFoundException 활성 상태의 정의를 찾지 못한 경우
     */
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

    /**
     * 고정비 마스터를 비활성화한다.
     *
     * @param command 비활성화 요청(정의 ID)
     * @param ownerUserId 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws FixedExpenseNotFoundException 활성 상태의 정의를 찾지 못한 경우
     */
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
