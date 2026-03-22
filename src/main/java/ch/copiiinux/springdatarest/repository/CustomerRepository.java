package ch.copiiinux.springdatarest.repository;

import ch.copiiinux.springdatarest.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
