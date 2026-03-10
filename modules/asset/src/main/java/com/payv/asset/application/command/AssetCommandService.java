package com.payv.asset.application.command;

import com.payv.asset.application.command.model.CreateAssetCommand;
import com.payv.asset.application.command.model.DeactivateAssetCommand;
import com.payv.asset.application.command.model.UpdateAssetCommand;
import com.payv.asset.application.exception.AssetNotFoundException;
import com.payv.asset.application.exception.DuplicateAssetNameException;
import com.payv.asset.domain.model.Asset;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.repository.AssetRepository;
import com.payv.common.error.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
/**
 * 자산(Asset)의 CUD(비활성) 명령을 처리하는 서비스.
 * - 자산의 생명주기 변경을 수행하고 저장소에 반영한다.
 * - 자산명 중복 금지, 소유자 검증 같은 규칙을 한 곳에서 강제해
 *   다른 BC(ledger/automation/reporting)의 참조 무결성을 유지한다.
 */
public class AssetCommandService {

    private final AssetRepository assetRepository;

    /**
     * 자산을 생성한다.
     *
     * @param command 생성 요청(이름, 자산 타입)
     * @param ownerUserId 자산 소유 사용자 ID
     * @return 생성된 자산 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws DuplicateAssetNameException 동일 소유자 내 중복 이름이 존재하는 경우
     */
    public AssetId create(CreateAssetCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        ensureUniqueAssetName(ownerUserId, command.getName(), null);

        Asset asset = Asset.create(ownerUserId, command.getName(), command.getAssetType());
        assetRepository.save(asset, ownerUserId);
        return asset.getId();
    }

    /**
     * 자산 정보를 수정한다.
     *
     * Business logic:
     * - 소유권 범위 내에서만 조회/수정
     * - 자기 자신을 제외한 이름 중복을 검증 후 변경
     *
     * @param command 수정 요청(자산 ID, 새 이름, 타입)
     * @param ownerUserId 자산 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws AssetNotFoundException 대상 자산이 없거나 소유자가 다른 경우
     * @throws DuplicateAssetNameException 수정 이름이 중복되는 경우
     */
    public void update(UpdateAssetCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Asset asset = assetRepository.findById(command.getAssetId(), ownerUserId)
                .orElseThrow(AssetNotFoundException::new);

        ensureUniqueAssetName(ownerUserId, command.getNewName(), asset.getId());

        asset.rename(command.getNewName());
        asset.changeType(command.getAssetType());
        assetRepository.save(asset, ownerUserId);
    }

    /**
     * 자산을 비활성화한다(소프트 삭제).
     *
     * @param command 비활성화 요청(자산 ID)
     * @param ownerUserId 자산 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws AssetNotFoundException 대상 자산이 없거나 소유자가 다른 경우
     */
    public void deactivate(DeactivateAssetCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Asset asset = assetRepository.findById(command.getAssetId(), ownerUserId)
                .orElseThrow(AssetNotFoundException::new);

        asset.deactivate();
        assetRepository.save(asset, ownerUserId);
    }

    private void ensureUniqueAssetName(String ownerUserId, String name, AssetId excludeId) {
        String normalized = Asset.normalizeName(name);
        assetRepository.findAllByOwner(ownerUserId).forEach(existing -> {
            if (excludeId != null && existing.getId().equals(excludeId)) return;
            if (existing.getName().equals(normalized)) {
                throw new DuplicateAssetNameException();
            }
        });
    }

    private static void requireOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            throw new InvalidRequestException("ownerUserId must not be blank");
        }
    }
}
