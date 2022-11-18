package pe.edu.unmsm;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import pe.edu.unmsm.exception.InsufficientBalanceException;
import pe.edu.unmsm.model.Account;
import pe.edu.unmsm.model.Bank;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;

class AccountTest {
    TestInfo testInfo;
    TestReporter testReporter;
    Account account;

    @BeforeAll
    static void beforeAll() {
        System.out.println("Inicializando el test");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Finalizando el test");
    }

    @BeforeEach
    void setUp(TestInfo testInfo, TestReporter testReporter) {
        System.out.println("Iniciando el método de prueba");
        testReporter.publishEntry("Ejecutando: " + testInfo.getDisplayName()
                + " " + testInfo.getTestMethod().map(Method::getName).orElse(null)
                + " con las etiquetas " + testInfo.getTags());
        this.testInfo = testInfo;
        this.testReporter = testReporter;
        this.account = new Account("Paul", new BigDecimal("1000.12345"));
    }

    @AfterEach
    void tearDown() {
        System.out.println("Finalizando el método de prueba");
    }

    @Nested
    @Tag("account")
    @DisplayName("Probando atributos de la cuenta")
    class AccountPersonBalanceTest {
        @Test
        @DisplayName("El nombre!")
        void testAccountPerson() {
            testReporter.publishEntry(testInfo.getTags().toString());
            if (testInfo.getTags().contains("account"))
                testReporter.publishEntry("El método tiene la etiqueta account!");

            String expected = "Paul";
            String actual = account.getPerson();

            assertNotNull(actual, () -> "La cuenta no puede ser nula");
            assertEquals(expected, actual, () -> "La persona de la cuenta no es la que se esperaba");
        }

        @Test
        @DisplayName("El saldo!")
        void testAccountBalance() {
            assertNotNull(account.getBalance());
            assertEquals(1000.12345, account.getBalance().doubleValue());
            assertFalse(account.getBalance().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("Probando cuentas iguales con el método equals")
        void testAccountReference() {
            Account account = new Account("John Doe", new BigDecimal("8900.9997"));
            Account account2 = new Account("John Doe", new BigDecimal("8900.9997"));

            assertEquals(account2, account);
        }
    }

    @Nested
    class AccountOperationsTest {
        @Test
        @Tag("account")
        void testAccountDebit() {
            account.debit(new BigDecimal(100));

            assertNotNull(account.getBalance());
            assertEquals(900, account.getBalance().intValue());
            assertEquals("900.12345", account.getBalance().toPlainString());
        }

        @Test
        @Tag("account")
        void testAccountCredit() {
            account.credit(new BigDecimal(100));

            assertNotNull(account.getBalance());
            assertEquals(1100, account.getBalance().intValue());
            assertEquals("1100.12345", account.getBalance().toPlainString());
        }

        @Test
        @Tags(value = {@Tag("account"), @Tag("bank")})
        void testTransferMoneyBetweenAccounts() {
            Account account1 = new Account("John Doe", new BigDecimal("2500"));
            Account account2 = new Account("Paul", new BigDecimal("1500.8989"));

            Bank bank = new Bank();
            bank.setName("Bank");
            bank.transfer(account2, account1, new BigDecimal(500));

            assertEquals("1000.8989", account2.getBalance().toPlainString());
            assertEquals("3000", account1.getBalance().toPlainString());
        }
    }


    @Test
    @Tags(value = {@Tag("account"), @Tag("error")})
    void testInsufficientBalanceException() {
        BigDecimal debit = new BigDecimal(1500);

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> account.debit(debit));

        String actual = exception.getMessage();
        String expected = "Balance insuficiente";

        assertEquals(expected, actual);
    }

    @Test
    @Tags(value = {@Tag("account"), @Tag("bank")})
    @Disabled("Se agrego método fail de Assertions")
    @DisplayName("Probando relaciones entre el banco y las cuentas con assertAll")
    void testRelationshipBetweenBankAndAccounts() {
        fail();
        Account account1 = new Account("John Doe", new BigDecimal("2500"));
        Account account2 = new Account("Paul", new BigDecimal("1500.8989"));

        Bank bank = new Bank();
        bank.setName("Banco del estado");
        bank.addAccount(account1);
        bank.addAccount(account2);
        bank.transfer(account2, account1, new BigDecimal(500));

        assertAll(
                () -> assertEquals(2, bank.getAccounts().size()),
                () -> assertEquals("1000.8989", account2.getBalance().toPlainString(), () -> "El valor del saldo de la cuenta2 no es el esperado"),
                () -> assertEquals("3000", account1.getBalance().toPlainString(), () -> "El valor del saldo de la cuenta1 no es el esperado"),
                () -> assertEquals("Banco del estado", account1.getBank().getName(), () -> "El banco no tiene las cuentas esperadas"),
                () -> assertTrue(bank.getAccounts().stream().anyMatch(acc -> acc.getPerson().equals("Paul"))),
                () -> bank.getAccounts()
                        .stream()
                        .filter(acc -> acc.getPerson().equals("Paul"))
                        .findFirst()
                        .ifPresent(acc -> assertEquals("Paul", acc.getPerson()))
        );
    }

    @Nested
    class OperatingSystemTest {
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testOnlyWindows() {
        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testLinuxAndMac() {
        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    @Nested
    class JavaVersionTest {
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void testOnlyJdk8() {
        }

        @Test
        @EnabledOnJre(JRE.JAVA_17)
        void testOnlyJdk17() {
        }

        @Test
        @DisabledOnJre(JRE.JAVA_17)
        void testNoJdk17() {
        }
    }

    @Nested
    class SystemPropertiesTest {
        @Test
        void testPrintSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k, v) -> System.out.println(k + ":" + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = "17.0.5")
        void testOnlyJavaVersion17() {
        }

        @Test
        @EnabledIfSystemProperty(named = "os.arch", matches = "amd64")
        void testOnlyOsArchAmd64() {
        }

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = "amd64")
        void testNoOsArchAmd64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "user.name", matches = "Paul")
        void testOnlyUsernamePaul() {
        }

        @Test
        @EnabledIfSystemProperty(named = "ENV", matches = "dev")
        void testSystemProperty() {
        }
    }

    @Nested
    class EnvironmentVariablesTest {
        @Test
        void testPrintEnvironmentVariables() {
            Map<String, String> env = System.getenv();
            env.forEach((k, v) -> System.out.println(k + " = " + v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "16")
        void testNumberOfProcessors() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENV", matches = "dev")
        void testEnabledEnvironmentVariable() {
        }

        @Test
        @DisabledIfEnvironmentVariable(named = "ENV", matches = "prod")
        void testDisabledEnvironmentVariable() {
        }
    }

    @Test
    @DisplayName("Probando el saldo de la cuenta solo en entorno de desarrollo usando assumeTrue!")
    void testAccountBalanceOnlyInDevEnv() {
        boolean isDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(isDev);

        assertNotNull(account.getBalance());
        assertEquals(1000.12345, account.getBalance().doubleValue());
        assertFalse(account.getBalance().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Probando el saldo de la cuenta solo en entorno de desarrollo usando assumingThat!")
    void testAccountBalanceOnlyInDevEnv2() {
        boolean isDev = "dev".equals(System.getProperty("ENV"));

        assumingThat(isDev, () -> {
            assertNotNull(account.getBalance());
            assertEquals(1000.12345, account.getBalance().doubleValue());
        });
        assertFalse(account.getBalance().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @DisplayName("Probando debito cuenta repetir!")
    @RepeatedTest(value = 5, name = "{displayName} - Repetición número {currentRepetition} de {totalRepetitions}")
    void testAccountDebitRepeat(RepetitionInfo info) {
        if (info.getCurrentRepetition() == 3) {
            System.out.println("Estamos en la repetición " + info.getCurrentRepetition());
        }

        account.debit(new BigDecimal(100));

        assertNotNull(account.getBalance());
        assertEquals(900, account.getBalance().intValue());
        assertEquals("900.12345", account.getBalance().toPlainString());
    }

    @Nested
    @Tag("param")
    class ParameterizedTestClass {
        @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
        @ValueSource(strings = {"100", "200", "300", "500", "700", "1000"})
        void testAccountDebitValueSource(String amount) {
            account.debit(new BigDecimal(amount));

            assertNotNull(account.getBalance());
            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700", "6,1000"})
        void testAccountDebitCsvSource(String index, String amount) {
            System.out.println(index + " -> " + amount);

            account.debit(new BigDecimal(amount));

            assertNotNull(account.getBalance());
            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvSource({"200,100,Paul,Paul", "250,200,Pepe,Pepe", "310,300,Maria,Maria", "510,500,Pepe,Pepe", "750,700,Lucas,Lucas", "1010,1000,Cata,Cata"})
        void testAccountDebitCsvSource2(String balance, String amount, String expected, String actual) {
            System.out.println(balance + " -> " + amount);

            account.setBalance(new BigDecimal(balance));
            account.debit(new BigDecimal(amount));
            account.setPerson(actual);

            assertNotNull(account.getBalance());
            assertNotNull(account.getPerson());
            assertEquals(expected, actual);
            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data.csv")
        void testAccountDebitCsvFileSource(String amount) {
            System.out.println(amount);

            account.debit(new BigDecimal(amount));

            assertNotNull(account.getBalance());
            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data2.csv")
        void testAccountDebitCsvFileSource2(String balance, String amount, String expected, String actual) {
            account.setBalance(new BigDecimal(balance));
            account.debit(new BigDecimal(amount));
            account.setPerson(actual);

            assertNotNull(account.getBalance());
            assertNotNull(account.getPerson());
            assertEquals(expected, actual);
            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Tag("param")
    @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
    @MethodSource("amountList")
    void testAccountDebitMethodSource(String amount) {
        System.out.println(amount);

        account.debit(new BigDecimal(amount));

        assertNotNull(account.getBalance());
        assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    static List<String> amountList() {
        return Arrays.asList("100", "200", "300", "500", "700", "1000");
    }

    @Nested
    @Tag("timeout")
    class TimeoutTest {
        @Test
        @Timeout(1)
        void testTimeout() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        @Test
        @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
        void testTimeout2() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(1000);
        }

        @Test
        void testTimeoutAssertions() {
            assertTimeout(Duration.ofSeconds(5), () -> TimeUnit.MILLISECONDS.sleep(900));
        }
    }
}