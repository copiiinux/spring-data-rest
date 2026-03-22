package ch.copiiinux.springdatarest.ctrl;

import ch.copiiinux.springdatarest.entity.Customer;
import ch.copiiinux.springdatarest.repository.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerRepositoryRestTest {
    private static final String URI_TEMPLATE = "/customers";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerRepository repository;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // The CommandLineRunner seeds a row on startup — wipe it first so every
        // test starts from a known, empty state.
        repository.deleteAll();
        Customer c = new Customer();
        c.setName("Jane Doe");
        customer = repository.save(c);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }
    // -------------------------------------------------------------------------
    // HEAD /customers
    // -------------------------------------------------------------------------

    @Test
    void headCollectionResource_shouldReturn204() throws Exception {
        mockMvc.perform(head(URI_TEMPLATE)).andExpect(status().isNoContent());
    }
    // -------------------------------------------------------------------------
    // GET /customers
    // -------------------------------------------------------------------------

    @Test
    void getCollectionResource_shouldReturnAllCustomers() throws Exception {
        mockMvc.perform(get(URI_TEMPLATE).accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$._embedded.customers[0].name").value(customer.getName()));
    }

    @Test
    void getCollectionResource_shouldReturnEmptyWhenNoCustomers() throws Exception {
        repository.deleteAll();
        mockMvc.perform(get(URI_TEMPLATE).accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$._embedded.customers").isEmpty());
    }
    // -------------------------------------------------------------------------
    // POST /customers
    // -------------------------------------------------------------------------

    @Test
    void postCollectionResource_shouldCreateAndReturn201() throws Exception {
        mockMvc.perform(post(URI_TEMPLATE).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                           {"name": "John Doe"}
                                                                                           """))
               .andExpect(status().isCreated())
               .andExpect(header().exists("Location"));
        assertThat(repository.count()).isEqualTo(2); // setUp customer + new one
    }
    // -------------------------------------------------------------------------
    // HEAD /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void headItemResource_shouldReturn204WhenExists() throws Exception {
        mockMvc.perform(head(URI_TEMPLATE + "/" + customer.getId())).andExpect(status().isNoContent());
    }

    @Test
    void headItemResource_shouldReturn404WhenNotExists() throws Exception {
        mockMvc.perform(head(URI_TEMPLATE + "/999")).andExpect(status().isNotFound());
    }
    // -------------------------------------------------------------------------
    // GET /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void getItemResource_shouldReturnCustomerWhenFound() throws Exception {
        mockMvc.perform(get(URI_TEMPLATE + "/" + customer.getId()).accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(customer.getName()));
    }

    @Test
    void getItemResource_shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get(URI_TEMPLATE + "/999").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }
    // -------------------------------------------------------------------------
    // PUT /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void putItemResource_shouldUpdateAndReturn200WhenExists() throws Exception {
        mockMvc.perform(put(URI_TEMPLATE + "/" + customer.getId()).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                   {"name": "Updated Name"}
                                                                                                                   """))
               .andExpect(status().isNoContent());
        assertThat(repository.findById(customer.getId())).isPresent()
                                                         .get()
                                                         .extracting(Customer::getName)
                                                         .isEqualTo("Updated Name");
        assertThat(repository.count()).isEqualTo(1); // unchanged
    }

    @Test
    void putItemResource_shouldReturn409WhenNotFound() throws Exception {
        mockMvc.perform(put(URI_TEMPLATE + "/999").contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                   {"name": "Brand New"}
                                                                                                   """))
               .andExpect(status().isConflict());
        assertThat(repository.count()).isEqualTo(1); // setUp customer + new one
    }
    // -------------------------------------------------------------------------
    // PATCH /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void patchItemResource_shouldUpdateAndReturn200WhenExists() throws Exception {
        mockMvc.perform(patch(URI_TEMPLATE + "/" + customer.getId()).contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                                     {"name": "Patched Name"}
                                                                                                                     """))
               .andExpect(status().isNoContent());
        assertThat(repository.findById(customer.getId())).isPresent()
                                                         .get()
                                                         .extracting(Customer::getName)
                                                         .isEqualTo("Patched Name");
        assertThat(repository.count()).isEqualTo(1); // unchanged
    }

    @Test
    void patchItemResource_shouldNotOverwriteFieldWhenAbsentFromBody() throws Exception {
        mockMvc.perform(patch(URI_TEMPLATE + "/" + customer.getId()).contentType(MediaType.APPLICATION_JSON)
                                                                    .content("{}")).andExpect(status().isNoContent());
        assertThat(repository.findById(customer.getId())).isPresent()
                                                         .get()
                                                         .extracting(Customer::getName)
                                                         .isEqualTo("Jane Doe");
        assertThat(repository.count()).isEqualTo(1); // unchanged
    }

    @Test
    void patchItemResource_shouldReturn404WhenNotExists() throws Exception {
        mockMvc.perform(patch(URI_TEMPLATE + "/999").contentType(MediaType.APPLICATION_JSON).content("""
                                                                                                     {"name": "Jane Doe"}
                                                                                                     """))
               .andExpect(status().isNotFound());
        assertThat(repository.count()).isEqualTo(1); // unchanged
    }
    // -------------------------------------------------------------------------
    // DELETE /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void deleteItemResource_shouldReturn204AndRemoveCustomer() throws Exception {
        mockMvc.perform(delete(URI_TEMPLATE + "/" + customer.getId())).andExpect(status().isNoContent());
        assertThat(repository.existsById(customer.getId())).isFalse();
        assertThat(repository.count()).isZero();
    }

    @Test
    void deleteItemResource_shouldReturn404WhenNotExists() throws Exception {
        mockMvc.perform(delete(URI_TEMPLATE + "/999")).andExpect(status().isNotFound());
        assertThat(repository.count()).isEqualTo(1); // unchanged
    }
}