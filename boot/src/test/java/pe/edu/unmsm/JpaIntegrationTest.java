package pe.edu.unmsm;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pe.edu.unmsm.model.Account;
import pe.edu.unmsm.repo.AccountRepo;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Tag("integration_jpa")
class JpaIntegrationTest {
    @Autowired
    AccountRepo accountRepo;

    @Test
    void testFindById() {
        Optional<Account> account = accountRepo.findById(1L);
        assertTrue(account.isPresent());
        assertEquals("Paul", account.orElseThrow().getPerson());
    }

    @Test
    void testFindByPerson() {
        Optional<Account> account = accountRepo.findByPerson("Paul");
        assertTrue(account.isPresent());
        assertEquals("Paul", account.orElseThrow().getPerson());
        assertEquals("1000.00", account.orElseThrow().getBalance().toPlainString());
    }

    @Test
    void testFindByPersonThrowException() {
        Optional<Account> account = accountRepo.findByPerson("Unknown");
        assertThrows(NoSuchElementException.class, account::orElseThrow);
        assertFalse(account.isPresent());
    }

    @Test
    void testFindAll() {
        List<Account> accounts = accountRepo.findAll();
        assertFalse(accounts.isEmpty());
        assertEquals(2, accounts.size());
    }

    @Test
    void testSave() {
        //given
        Account newAccount = new Account(null, "Pepe", new BigDecimal("3000"));

        //when
        Account savedAccount = accountRepo.save(newAccount);

        //then
        assertEquals("Pepe", savedAccount.getPerson());
        assertEquals("3000", savedAccount.getBalance().toPlainString());
    }

    @Test
    void testUpdate() {
        //given
        Account newAccount = new Account(null, "Pepe", new BigDecimal("3000"));

        //when
        Account savedAccount = accountRepo.save(newAccount);

        //then
        assertEquals("Pepe", savedAccount.getPerson());
        assertEquals("3000", savedAccount.getBalance().toPlainString());

        //given
        savedAccount.setBalance(new BigDecimal("3800"));

        //when
        Account updatedAccount = accountRepo.save(savedAccount);

        //then
        assertEquals("Pepe", updatedAccount.getPerson());
        assertEquals("3800", updatedAccount.getBalance().toPlainString());
    }

    @Test
    void testDelete() {
        Account account = accountRepo.findById(2L).orElseThrow();
        assertEquals("Fernando", account.getPerson());

        accountRepo.delete(account);

        assertEquals(1, accountRepo.count());
    }
}
