package org.apache.commons.configuration2;

import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.web.ServletConfiguration;
import org.junit.jupiter.api.Test;

class TestConfiguration {

    @Test
    void testConfiguration() throws ConfigurationException {
        Configurations configManager = new Configurations();
        Configuration config = configManager.properties("src/test/resources/config/test.properties");

        System.out.println(config.containsValue("jndivalue2"));
    }
}
