package com.github.shaart.jenkins2discord.notification.factory;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory {

  @NonNull
  @Override
  public PropertySource<?> createPropertySource(@Nullable String name,
      EncodedResource encodedResource) {
    YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
    Resource resource = encodedResource.getResource();
    factory.setResources(resource);

    Properties properties = factory.getObject();

    if (properties == null) {
      throw new IllegalStateException("properties were null");
    }

    String filename = resource.getFilename();
    if (filename == null) {
      throw new IllegalStateException("filename was null");
    }
    return new PropertiesPropertySource(filename, properties);
  }
}
