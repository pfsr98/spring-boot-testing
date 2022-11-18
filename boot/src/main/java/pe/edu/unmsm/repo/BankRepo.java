package pe.edu.unmsm.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unmsm.model.Bank;

public interface BankRepo extends JpaRepository<Bank, Long> {
}
