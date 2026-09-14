package com.novabank.features.account.entity;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import com.novabank.features.account.repository.AccountRepository;
import com.novabank.features.customer.entity.Customer;
import com.novabank.features.customer.repository.CustomerRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class AccountTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void should_throw_optimistic_lock_exception_when_two_transactions_update_same_account() {
        // 1. Criar Customer real (para satisfazer FK)
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Optimistic");
        customer.setEmail("test.optimistic@novabank.com");
        customer.setActive(true);
        customer = customerRepository.saveAndFlush(customer);

        // 2. Persistir conta com saldo inicial
        Account account = new Account();
        account.setCustomerId(customer.getId());
        account.setAccountNumber("TEST-OPT-001");
        account.setAccountType("CHECKING");
        account.setBalance(new BigDecimal("1000.00"));
        account = accountRepository.saveAndFlush(account);

        // 3. Carregar duas "sessões" independentes
        entityManager.clear();
        Account session1 = accountRepository.findById(account.getId()).orElseThrow();
        entityManager.clear();
        Account session2 = accountRepository.findById(account.getId()).orElseThrow();

        // 4. Ambas têm version = 0
        assertThat(session1.getVersion()).isZero();
        assertThat(session2.getVersion()).isZero();

        // 5. Session1 atualiza e persiste → version passa a 1
        session1.setBalance(new BigDecimal("900.00"));
        accountRepository.saveAndFlush(session1);

        // 5b. Forçar version desatualizada diretamente na BD
        // (simula outro processo a atualizar a mesma conta entretanto)
        entityManager
                .createNativeQuery(
                        "UPDATE accounts.accounts SET version = version + 1 WHERE id = :id")
                .setParameter("id", account.getId()).executeUpdate();

        // 5c. Limpar persistence context para que session2 releia o estado da BD
        entityManager.clear();

        // 6. Session2 tenta atualizar → deve falhar com OptimisticLock
        session2.setBalance(new BigDecimal("800.00"));
        assertThatThrownBy(() -> accountRepository.saveAndFlush(session2))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
