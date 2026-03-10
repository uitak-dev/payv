package com.payv.iam.application.query;

import com.payv.iam.application.exception.OwnershipDeniedException;
import com.payv.iam.application.query.model.UserProfileView;
import com.payv.iam.domain.model.UserId;
import com.payv.iam.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
/**
 * 사용자 조회/권한 검증을 담당하는 애플리케이션 서비스.
 * - 사용자 프로필 조회와 소유자 일치 검증을 제공한다.
 * - 다른 BC가 IAM 규칙(소유권)을 재구현하지 않도록, 단일 지점에서 권한 검증을 수행한다.
 */
public class IamQueryService {

    private final UserRepository userRepository;

    /**
     * 사용자 프로필을 조회한다.
     * - 사용자 ID로 조회 후, 화면/ACL 용 읽기 모델로 변환한다.
     * - 도메인 객체를 외부에 직접 노출하지 않고, 최소 정보만 전달한다.
     *
     * @param userId 조회할 사용자 식별자
     * @return 사용자 프로필. 입력이 비어 있거나 사용자가 없으면 {@link Optional#empty()}
     */
    public Optional<UserProfileView> getUserProfile(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findById(UserId.of(userId))
                .map(user -> new UserProfileView(
                        user.getId().getValue(),
                        user.getEmail(),
                        user.getDisplayName()
                ));
    }

}
