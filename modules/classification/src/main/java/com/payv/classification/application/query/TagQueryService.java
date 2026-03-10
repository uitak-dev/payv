package com.payv.classification.application.query;

import com.payv.classification.application.query.model.TagView;
import com.payv.classification.domain.model.Tag;
import com.payv.classification.domain.model.TagId;
import com.payv.classification.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 태그 조회 서비스.
 * - 태그 목록/상세/ID 기반 이름 맵 조회를 제공한다.
 * - 다른 BC(Ledger/Reporting)에서 태그명을 빠르게 역조회할 수 있는 공통 조회 계약을 제공한다.
 */
public class TagQueryService {

    private final TagRepository tagRepository;

    /**
     * 소유자의 전체 태그를 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @return 태그 뷰 목록
     */
    public List<TagView> getAll(String ownerUserId) {
        List<Tag> tags = tagRepository.findAllByOwner(ownerUserId);
        return tags.stream().map(TagView::from).collect(Collectors.toList());
    }

    /**
     * 태그 단건을 조회한다.
     *
     * @param tagId 태그 ID
     * @param ownerUserId 소유 사용자 ID
     * @return 태그 뷰. 없으면 {@link Optional#empty()}
     */
    public Optional<TagView> get(TagId tagId, String ownerUserId) {
        return tagRepository.findById(tagId, ownerUserId).map(TagView::from);
    }

}
