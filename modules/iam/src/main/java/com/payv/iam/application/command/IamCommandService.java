package com.payv.iam.application.command;

import com.payv.iam.application.command.model.SignUpCommand;
import com.payv.iam.application.exception.EmailAlreadyExistsException;
import com.payv.common.error.InvalidRequestException;
import com.payv.iam.domain.model.User;
import com.payv.iam.domain.model.UserId;
import com.payv.iam.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
/**
 * 사용자 쓰기 작업을 담당하는 애플리케이션 서비스.
 * - 회원가입 요청을 도메인 엔티티로 변환하고 저장한다.
 * - 이메일 중복/비밀번호 인코딩 같은 보안·무결성 규칙을 일관되게 관리한다.
 */
public class IamCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 신규 사용자를 등록한다.
     * - 이메일 정규화 후 중복 여부를 확인하고, 비밀번호를 인코딩하여 저장한다.
     * - 원문 비밀번호 저장을 방지하고, 동일 이메일 중복 가입을 차단해 인증 데이터의 정합성을 유지한다.
     *
     * @param command 회원가입 입력 정보(이메일, 비밀번호, 표시 이름)
     * @return 생성된 사용자 식별자
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws EmailAlreadyExistsException 동일 이메일이 이미 존재하는 경우
     * @throws InvalidRequestException 비밀번호가 공백이거나 유효하지 않은 경우
     */
    public UserId signUp(SignUpCommand command) {
        Objects.requireNonNull(command, "command");

        String normalizedEmail = User.normalizeEmail(command.getEmail());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        User user = User.create(
                normalizedEmail,
                passwordEncoder.encode(requireRawPassword(command.getPassword())),
                command.getDisplayName()
        );

        userRepository.save(user);
        return user.getId();
    }

    private static String requireRawPassword(String rawPassword) {
        String ret = (rawPassword == null) ? null : rawPassword.trim();
        if (ret == null || ret.isEmpty()) {
            throw new InvalidRequestException("password must not be blank");
        }
        return ret;
    }
}
