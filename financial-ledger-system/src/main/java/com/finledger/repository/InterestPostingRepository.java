package com.finledger.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import com.finledger.entity.InterestPosting;

public interface InterestPostingRepository extends JpaRepository<InterestPosting, Long> {
    boolean existsByAccountIdAndPeriod(Long accountId, LocalDate period);
}
