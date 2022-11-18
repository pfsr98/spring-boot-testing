package pe.edu.unmsm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unmsm.model.Account;
import pe.edu.unmsm.model.Bank;
import pe.edu.unmsm.repo.AccountRepo;
import pe.edu.unmsm.repo.BankRepo;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepo accountRepo;
    private final BankRepo bankRepo;

    public AccountServiceImpl(AccountRepo accountRepo, BankRepo bankRepo) {
        this.accountRepo = accountRepo;
        this.bankRepo = bankRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Account findById(Long id) {
        return accountRepo.findById(id).orElseThrow();
    }

    @Override
    @Transactional
    public Account save(Account account) {
        return accountRepo.save(account);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        accountRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalTransfers(Long bankId) {
        Bank bank = bankRepo.findById(bankId).orElseThrow();
        return bank.getTotalTransfers();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long accountId) {
        Account account = accountRepo.findById(accountId).orElseThrow();
        return account.getBalance();
    }

    @Override
    @Transactional
    public void transfer(Long bankId, Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
        Account sourceAccount = accountRepo.findById(sourceAccountId).orElseThrow();
        sourceAccount.debit(amount);
        accountRepo.save(sourceAccount);

        Account targetAccount = accountRepo.findById(targetAccountId).orElseThrow();
        targetAccount.credit(amount);
        accountRepo.save(targetAccount);

        Bank bank = bankRepo.findById(1L).orElseThrow();
        int totalTransfers = bank.getTotalTransfers();
        bank.setTotalTransfers(++totalTransfers);
        bankRepo.save(bank);
    }
}