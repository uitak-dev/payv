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
public class AssetCommandService {

    private final AssetRepository assetRepository;

    public AssetId create(CreateAssetCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        ensureUniqueAssetName(ownerUserId, command.getName(), null);

        Asset asset = Asset.create(ownerUserId, command.getName(), command.getAssetType());
        assetRepository.save(asset, ownerUserId);
        return asset.getId();
    }

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
