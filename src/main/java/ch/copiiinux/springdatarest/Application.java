package ch.copiiinux.springdatarest;

import ch.copiiinux.springdatarest.entity.Customer;
import ch.copiiinux.springdatarest.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.NoSuchElementException;

@SpringBootApplication
public class Application {

    static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner runner(final CustomerRepository repository) {
        return _ -> {
            final Customer person = new Customer();
            person.setName("John");
            repository.save(person);
            repository.findById(person.getId()).orElseThrow(NoSuchElementException::new);
        };
    }

}
