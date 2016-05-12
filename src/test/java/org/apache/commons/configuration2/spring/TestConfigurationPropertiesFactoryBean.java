package org.apache.commons.configuration2.spring;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.StringReader;
import java.util.Properties;

/**
 * Spring FactoryBean test.
 *
 */
public class TestConfigurationPropertiesFactoryBean {

    private ConfigurationPropertiesFactoryBean configurationFactory;

    @Before
    public void setUp() {
        configurationFactory = new ConfigurationPropertiesFactoryBean();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAfterPropertiesSet() throws Exception {
        configurationFactory.afterPropertiesSet();
    }

    @Test
    public void testGetObject() throws Exception {
        configurationFactory.setConfigurations(new Configuration[] { new BaseConfiguration() });
        Assert.assertNull(configurationFactory.getObject());
        configurationFactory.afterPropertiesSet();
        Assert.assertNotNull(configurationFactory.getObject());
    }

    @Test
    public void testMergeConfigurations() throws Exception {
        Configuration one = new BaseConfiguration();
        one.setProperty("foo", "bar");
        String properties = "## some header \n" + "foo = bar1\n" + "bar = foo\n";

        PropertiesConfiguration two = new PropertiesConfiguration();
        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();
        layout.load(two, new StringReader(properties));

        configurationFactory.setConfigurations(new Configuration[] { one, two });
        configurationFactory.afterPropertiesSet();
        Properties props = configurationFactory.getObject();
        Assert.assertEquals("foo", props.getProperty("bar"));
        Assert.assertEquals("bar", props.getProperty("foo"));
    }

    @Test
    public void testLoadResources() throws Exception {
        configurationFactory.setLocations(new Resource[] { new ClassPathResource("testConfigurationFactoryBean.file") });
        configurationFactory.setConfigurations(new Configuration[] { new BaseConfiguration() });
        configurationFactory.afterPropertiesSet();

        Properties props = configurationFactory.getObject();
        Assert.assertEquals("duke", props.getProperty("java"));
    }

    @Test
    public void testInitialConfiguration() throws Exception {
        configurationFactory = new ConfigurationPropertiesFactoryBean(new BaseConfiguration());
        configurationFactory.afterPropertiesSet();
        Assert.assertNotNull(configurationFactory.getConfiguration());
    }
}
