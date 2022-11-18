package pe.edu.unmsm.service;

import pe.edu.unmsm.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    List<Account> findAll();

    Account findById(Long id);

    Account save(Account account);

    void deleteById(Long id);

    int getTotalTransfers(Long bankId);

    BigDecimal getBalance(Long accountId);

    void transfer(Long bankId, Long sourceAccountId, Long targetAccountId, BigDecimal amount);
}
