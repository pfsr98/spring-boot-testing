package pe.edu.unmsm;

import pe.edu.unmsm.model.Account;
import pe.edu.unmsm.model.Bank;

import java.math.BigDecimal;
import java.util.Optional;

public class Data {
    private Data() {
        throw new IllegalStateException("Utility class");
    }

    public static Optional<Account> createAccount001() {
        return Optional.of(new Account(1L, "Paul", new BigDecimal("1000")));
    }

    public static Optional<Account> createAccount002() {
        return Optional.of(new Account(2L, "Fernando", new BigDecimal("2000")));
    }

    public static Optional<Bank> createBank() {
        return Optional.of(new Bank(1L, "Banco Financiero", 0));
    }
}
