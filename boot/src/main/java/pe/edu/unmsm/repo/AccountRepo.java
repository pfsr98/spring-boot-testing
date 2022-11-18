package pe.edu.unmsm.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.unmsm.model.Account;

import java.util.Optional;

public interface AccountRepo extends JpaRepository<Account, Long> {
    @Query("SELECT A FROM Account A WHERE A.person = ?1")
    Optional<Account> findByPerson(String person);
}