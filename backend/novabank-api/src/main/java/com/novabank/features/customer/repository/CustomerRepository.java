package com.novabank.features.customer.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.novabank.features.customer.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    Page<Customer> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    boolean existsByDocumentNumber(String documentNumber);

    Optional<Customer> findByDocumentNumber(String documentNumber);

    boolean existsByEmail(String email);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);
}
