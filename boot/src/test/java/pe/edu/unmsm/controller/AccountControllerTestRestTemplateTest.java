package pe.edu.unmsm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import pe.edu.unmsm.dto.TransactionDto;
import pe.edu.unmsm.model.Account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration_rt")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTestRestTemplateTest {
    @Autowired
    private TestRestTemplate client;

    @LocalServerPort
    private int port;

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

        //when
        ResponseEntity<String> responseEntity = client.postForEntity(buildUrl("/api/accounts/transfer"), transactionDto, String.class);

        //then
        String json = responseEntity.getBody();
        System.out.println(port);
        System.out.println(json);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(json);
        assertTrue(json.contains("Transferencia realizada con éxito!"));
        assertTrue(json.contains("{\"bankId\":1,\"sourceAccountId\":1,\"targetAccountId\":2,\"amount\":100},\"status\":\"OK\"}"));

        JsonNode jsonNode = objectMapper.readTree(json);

        assertEquals("Transferencia realizada con éxito!", jsonNode.path("message").asText());
        assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
        assertEquals(1L, jsonNode.path("transaction").path("sourceAccountId").asLong());
        assertEquals("100", jsonNode.path("transaction").path("amount").asText());

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("message", "Transferencia realizada con éxito!");
        response.put("transaction", transactionDto);

        assertEquals(objectMapper.writeValueAsString(response), json);
    }

    @Test
    @Order(2)
    void testDetails() {
        //when
        ResponseEntity<Account> responseEntity = client.getForEntity(buildUrl("/api/accounts/1"), Account.class);

        //then
        Account account = responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(account);
        assertEquals("Paul", account.getPerson());
        assertEquals("900.00", account.getBalance().toPlainString());
        assertEquals(new Account(1L, "Paul", new BigDecimal("900.00")), account);
    }

    @Test
    @Order(3)
    void testList() throws JsonProcessingException {
        //when
        ResponseEntity<Account[]> responseEntity = client.getForEntity(buildUrl("/api/accounts"), Account[].class);

        //then
        assertNotNull(responseEntity.getBody());

        List<Account> accounts = Arrays.asList(responseEntity.getBody());

        assertEquals(2, accounts.size());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(1L, accounts.get(0).getId());
        assertEquals("Paul", accounts.get(0).getPerson());
        assertEquals("900.00", accounts.get(0).getBalance().toPlainString());
        assertEquals(2L, accounts.get(1).getId());
        assertEquals("Fernando", accounts.get(1).getPerson());
        assertEquals("2100.00", accounts.get(1).getBalance().toPlainString());

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(accounts));

        assertEquals(1L, jsonNode.get(0).path("id").asLong());
        assertEquals("Paul", jsonNode.get(0).path("person").asText());
        assertEquals("900.0", jsonNode.get(0).path("balance").asText());
        assertEquals(2L, jsonNode.get(1).path("id").asLong());
        assertEquals("Fernando", jsonNode.get(1).path("person").asText());
        assertEquals("2100.0", jsonNode.get(1).path("balance").asText());
    }

    @Test
    @Order(4)
    void testSave() {
        //given
        Account account = new Account(null, "Pepa", new BigDecimal("3800"));

        //when
        ResponseEntity<Account> responseEntity = client.postForEntity(buildUrl("/api/accounts"), account, Account.class);

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        Account createdAccount = responseEntity.getBody();

        assertNotNull(createdAccount);
        assertEquals(3L, createdAccount.getId());
        assertEquals("Pepa", createdAccount.getPerson());
        assertEquals("3800", createdAccount.getBalance().toPlainString());
    }

    @Test
    @Order(5)
    void testDelete() {
        ResponseEntity<Account[]> responseEntity = client.getForEntity(buildUrl("/api/accounts"), Account[].class);
        assertNotNull(responseEntity.getBody());
        List<Account> accounts = Arrays.asList(responseEntity.getBody());
        assertEquals(3, accounts.size());

//        client.delete(buildUrl("/api/accounts/3"));

        Map<String, Long> pathVariables = new HashMap<>();
        pathVariables.put("id", 3L);

        ResponseEntity<Void> exchange = client.exchange(buildUrl("/api/accounts/{id}"), HttpMethod.DELETE, null, Void.class, pathVariables);
        assertEquals(HttpStatus.NO_CONTENT, exchange.getStatusCode());
        assertFalse(exchange.hasBody());

        responseEntity = client.getForEntity(buildUrl("/api/accounts"), Account[].class);
        assertNotNull(responseEntity.getBody());
        accounts = Arrays.asList(responseEntity.getBody());
        assertEquals(2, accounts.size());

        ResponseEntity<Account> responseEntityDetails = client.getForEntity(buildUrl("/api/accounts/3"), Account.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntityDetails.getStatusCode());
        assertFalse(responseEntityDetails.hasBody());
    }

    private String buildUrl(String uri) {
        return "http://localhost:" + port + uri;
    }
}
