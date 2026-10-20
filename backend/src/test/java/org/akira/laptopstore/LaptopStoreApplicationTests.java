package org.akira.laptopstore;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires a live PostgreSQL database instance at localhost:5432")
class LaptopStoreApplicationTests {

    @Test
    void contextLoads() {
    }

}
