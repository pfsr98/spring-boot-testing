package pe.edu.unmsm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.unmsm.Data;
import pe.edu.unmsm.dto.TransactionDto;
import pe.edu.unmsm.model.Account;
import pe.edu.unmsm.service.AccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDetails() throws Exception {
        //given
        when(accountService.findById(1L)).thenReturn(Data.createAccount001().orElseThrow());

        //when
        mockMvc.perform(get("/api/accounts/1").contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.person").value("Paul"))
                .andExpect(jsonPath("$.balance").value("1000"));

        verify(accountService).findById(1L);
    }

    @Test
    void testTransfer() throws Exception {
        //given
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setBankId(1L);
        transactionDto.setSourceAccountId(1L);
        transactionDto.setTargetAccountId(2L);
        transactionDto.setAmount(new BigDecimal("100"));

        System.out.println(objectMapper.writeValueAsString(transactionDto));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("message", "Transferencia realizada con éxito!");
        response.put("transaction", transactionDto);

        System.out.println(objectMapper.writeValueAsString(response));

        //when
        mockMvc.perform(post("/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDto)))
                //then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.message").value("Transferencia realizada con éxito!"))
                .andExpect(jsonPath("$.transaction.sourceAccountId").value(transactionDto.getSourceAccountId()))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void testList() throws Exception {
        //given
        List<Account> accounts = Arrays.asList(Data.createAccount001().orElseThrow(), Data.createAccount002().orElseThrow());
        when(accountService.findAll()).thenReturn(accounts);

        //when
        mockMvc.perform(get("/api/accounts").contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].person").value("Paul"))
                .andExpect(jsonPath("$[0].balance").value("1000"))
                .andExpect(jsonPath("$[1].person").value("Fernando"))
                .andExpect(jsonPath("$[1].balance").value("2000"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(content().json(objectMapper.writeValueAsString(accounts)));

        verify(accountService).findAll();
    }

    @Test
    void testSave() throws Exception {
        //given
        Account account = new Account(null, "Pepe", new BigDecimal("3000"));
        when(accountService.save(any())).then(invocationOnMock -> {
            Account acc = invocationOnMock.getArgument(0);
            acc.setId(3L);
            return acc;
        });

        //when
        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(account)))
                //then
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.person", is("Pepe")))
                .andExpect(jsonPath("$.balance", is(3000)));

        verify(accountService).save(any());
    }
}