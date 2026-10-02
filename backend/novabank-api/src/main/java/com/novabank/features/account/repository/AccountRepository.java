package com.novabank.features.account.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.novabank.features.account.entity.Account;
import java.util.List;


@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Page<Account> findByCustomerId(Long customerId, Pageable pageable);

    Page<Account> findByIsActive(boolean isActive, Pageable pageable);
}
