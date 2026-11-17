package ng.sciCloud.library.repository;

import ng.sciCloud.library.model.CodePurpose;
import ng.sciCloud.library.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findFirstByUserIdAndPurposeAndUsedFalseOrderByIdDesc(Long userId, CodePurpose purpose);

    Optional<VerificationCode> findFirstByUserIdAndPurposeOrderByIdDesc(Long userId, CodePurpose purpose);

    List<VerificationCode> findByUserIdAndPurposeAndUsedFalse(Long userId, CodePurpose purpose);
}
