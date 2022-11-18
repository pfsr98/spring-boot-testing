package pe.edu.unmsm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pe.edu.unmsm.exception.InsufficientMoneyException;
import pe.edu.unmsm.model.Account;
import pe.edu.unmsm.model.Bank;
import pe.edu.unmsm.repo.AccountRepo;
import pe.edu.unmsm.repo.BankRepo;
import pe.edu.unmsm.service.AccountService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pe.edu.unmsm.Data.*;

@SpringBootTest
class ApplicationTest {
    @MockBean
    AccountRepo accountRepo;

    @MockBean
    BankRepo bankRepo;

    @Autowired
    AccountService accountService;

    @BeforeEach
    void setUp() {
        /*accountRepo = mock(AccountRepo.class);
        bankRepo = mock(BankRepo.class);
        accountService = new AccountServiceImpl(accountRepo, bankRepo);*/
    }

    @Test
    void contextLoads() {
        when(accountRepo.findById(1L)).thenReturn(createAccount001());
        when(accountRepo.findById(2L)).thenReturn(createAccount002());
        when(bankRepo.findById(1L)).thenReturn(createBank());

        BigDecimal sourceBalance = accountService.getBalance(1L);
        BigDecimal targetBalance = accountService.getBalance(2L);

        assertEquals("1000", sourceBalance.toPlainString());
        assertEquals("2000", targetBalance.toPlainString());

        accountService.transfer(1L, 1L, 2L, new BigDecimal("100"));
        sourceBalance = accountService.getBalance(1L);
        targetBalance = accountService.getBalance(2L);
        int total = accountService.getTotalTransfers(1L);

        assertEquals("900", sourceBalance.toPlainString());
        assertEquals("2100", targetBalance.toPlainString());
        assertEquals(1, total);

        verify(accountRepo, times(3)).findById(1L);
        verify(accountRepo, times(3)).findById(2L);
        verify(accountRepo, times(2)).save(any(Account.class));
        verify(bankRepo, times(2)).findById(1L);
        verify(bankRepo).save(any(Bank.class));
        verify(accountRepo, times(6)).findById(anyLong());
        verify(accountRepo, never()).findAll();
    }

    @Test
    void contextLoads2() {
        when(accountRepo.findById(1L)).thenReturn(createAccount001());
        when(accountRepo.findById(2L)).thenReturn(createAccount002());
        when(bankRepo.findById(1L)).thenReturn(createBank());

        BigDecimal sourceBalance = accountService.getBalance(1L);
        BigDecimal targetBalance = accountService.getBalance(2L);
        BigDecimal transferAmount = new BigDecimal("1200");

        assertEquals("1000", sourceBalance.toPlainString());
        assertEquals("2000", targetBalance.toPlainString());
        assertThrows(InsufficientMoneyException.class, () -> accountService.transfer(1L, 1L, 2L, transferAmount));

        sourceBalance = accountService.getBalance(1L);
        targetBalance = accountService.getBalance(2L);
        int total = accountService.getTotalTransfers(1L);

        assertEquals("1000", sourceBalance.toPlainString());
        assertEquals("2000", targetBalance.toPlainString());
        assertEquals(0, total);

        verify(accountRepo, times(3)).findById(1L);
        verify(accountRepo, times(2)).findById(2L);
        verify(accountRepo, never()).save(any(Account.class));
        verify(bankRepo, times(1)).findById(1L);
        verify(bankRepo, never()).save(any(Bank.class));
        verify(accountRepo, times(5)).findById(anyLong());
        verify(accountRepo, never()).findAll();
    }

    @Test
    void contextLoads3() {
        when(accountRepo.findById(1L)).thenReturn(createAccount001());

        Account account1 = accountService.findById(1L);
        Account account2 = accountService.findById(1L);

        assertSame(account1, account2);
        assertEquals("Paul", account1.getPerson());
        assertEquals("Paul", account1.getPerson());
        verify(accountRepo, times(2)).findById(1L);
    }

    @Test
    void testFindAll() {
        //given
        List<Account> accounts = Arrays.asList(createAccount001().orElseThrow(), createAccount002().orElseThrow());
        when(accountRepo.findAll()).thenReturn(accounts);

        //when
        List<Account> response = accountService.findAll();

        //then
        assertFalse(response.isEmpty());
        assertEquals(2, response.size());
        assertTrue(response.contains(createAccount002().orElseThrow()));

        verify(accountRepo).findAll();
    }

    @Test
    void testSave() {
        //given
        Account account = new Account(null, "Pepe", new BigDecimal("3000"));
        when(accountRepo.save(any())).then(invocationOnMock -> {
            Account acc = invocationOnMock.getArgument(0);
            acc.setId(3L);
            return acc;
        });

        //when
        Account response = accountService.save(account);

        //then
        assertEquals("Pepe", account.getPerson());
        assertEquals(3, account.getId());
        assertEquals("3000", account.getBalance().toPlainString());

        verify(accountRepo).save(any());
    }
}
