package ch.copiiinux.springdatarest.repository;

import ch.copiiinux.springdatarest.entity.Customer;
import org.springframework.data.repository.ListCrudRepository;

public interface CustomerRepository extends ListCrudRepository<Customer, Long> {
}
