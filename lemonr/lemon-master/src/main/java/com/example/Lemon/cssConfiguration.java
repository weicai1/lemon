package com.example.Lemon;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class cssConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                "/images/**",
                "/profileimage/**",
                "/css/**",
                "/js/**",
                "/templates/**")
                .addResourceLocations(
                        "classpath:/static/images/",
                        "classpath:/templates/profileimage/",
                        "classpath:/static/css/",
                        "classpath:/static/js/",
                        "classpath:/templates/");

    }

}