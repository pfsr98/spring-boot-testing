package pe.edu.unmsm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pe.edu.unmsm.dto.TransactionDto;
import pe.edu.unmsm.model.Account;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration_wc")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerWebTestClientTest {
    @Autowired
    private WebTestClient client;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @Order(1)
    void testTransfer() throws JsonProcessingException {
        //given
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setBankId(1L);
        transactionDto.setSourceAccountId(1L);
        transactionDto.setTargetAccountId(2L);
        transactionDto.setAmount(new BigDecimal("100"));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("message", "Transferencia realizada con éxito!");
        response.put("transaction", transactionDto);

        //when
        client.post()
                .uri("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transactionDto)
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
//                .expectBody(String.class)
                .consumeWith(res -> {
                    JsonNode json;
                    try {
                        json = objectMapper.readTree(res.getResponseBody());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    assertEquals("Transferencia realizada con éxito!", json.path("message").asText());
                    assertEquals(1L, json.path("transaction").path("sourceAccountId").asLong());
                    assertEquals(LocalDate.now().toString(), json.path("date").asText());
                    assertEquals("100", json.path("transaction").path("amount").asText());
                })
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.message").value(is("Transferencia realizada con éxito!"))
                .jsonPath("$.message").value(value -> assertEquals("Transferencia realizada con éxito!", value))
                .jsonPath("$.message").isEqualTo("Transferencia realizada con éxito!")
                .jsonPath("$.transaction.sourceAccountId").isEqualTo(transactionDto.getSourceAccountId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
                .json(objectMapper.writeValueAsString(response));
    }

    @Test
    @Order(2)
    void testDetails() throws JsonProcessingException {
        //given
        Account account = new Account(1L, "Paul", new BigDecimal("900"));

        //when
        client.get()
                .uri("/api/accounts/1")
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.person").isEqualTo("Paul")
                .jsonPath("$.balance").isEqualTo(900)
                .json(objectMapper.writeValueAsString(account));
    }

    @Test
    @Order(3)
    void testDetails2() {
        //when
        client.get()
                .uri("/api/accounts/2")
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Account.class)
                .consumeWith(response -> {
                    Account account = response.getResponseBody();
                    assertNotNull(account);
                    assertEquals("Fernando", account.getPerson());
                    assertEquals("2100.00", account.getBalance().toPlainString());
                });
    }

    @Test
    @Order(4)
    void testListar() {
        //when
        client.get()
                .uri("/api/accounts")
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$").value(hasSize(2))
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].person").isEqualTo("Paul")
                .jsonPath("$[0].balance").isEqualTo(900)
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].person").isEqualTo("Fernando")
                .jsonPath("$[1].balance").isEqualTo(2100);
    }

    @Test
    @Order(4)
    void testListar2() {
        //when
        client.get()
                .uri("/api/accounts")
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Account.class)
                .consumeWith(response -> {
                    List<Account> accounts = response.getResponseBody();
                    assertNotNull(accounts);
                    assertEquals(2, accounts.size());
                    assertEquals(1L, accounts.get(0).getId());
                    assertEquals("Paul", accounts.get(0).getPerson());
                    assertEquals("900.0", accounts.get(0).getBalance().toPlainString());
                    assertEquals(2L, accounts.get(1).getId());
                    assertEquals("Fernando", accounts.get(1).getPerson());
                    assertEquals("2100.0", accounts.get(1).getBalance().toPlainString());
                })
                .hasSize(2)
                .value(hasSize(2));
    }

    @Test
    @Order(6)
    void testSave() {
        //given
        Account account = new Account(null, "Pepe", new BigDecimal("3000"));

        //when
        client.post()
                .uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(account)
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(3)
                .jsonPath("$.person").isEqualTo("Pepe")
                .jsonPath("$.person").value(is("Pepe"))
                .jsonPath("$.balance").isEqualTo(3000);
    }

    @Test
    @Order(7)
    void testSave2() {
        //given
        Account account = new Account(null, "Pepa", new BigDecimal("3500"));

        //when
        client.post()
                .uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(account)
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Account.class)
                .consumeWith(response -> {
                    Account acc = response.getResponseBody();
                    assertNotNull(acc);
                    assertEquals(4L, acc.getId());
                    assertEquals("Pepa", acc.getPerson());
                    assertEquals("3500", acc.getBalance().toPlainString());
                });
    }

    @Test
    @Order(8)
    void testDelete() {
        client.get()
                .uri("/api/accounts")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Account.class)
                .hasSize(4);

        client.delete()
                .uri("/api/accounts/3")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        client.get()
                .uri("/api/accounts")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Account.class)
                .hasSize(3);

        client.get()
                .uri("/api/accounts/3")
                .exchange()
//                .expectStatus().is5xxServerError();
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }
}
