package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.BankAccountsUser;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BankAccountRepository extends JpaRepository<BankAccountsUser, Long> {
    Optional<BankAccountsUser> findByBankAccountId(String bankAccountId);

}
