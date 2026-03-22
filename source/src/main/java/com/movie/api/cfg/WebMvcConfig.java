package com.movie.api.cfg;

import com.movie.api.cfg.component.LogInterceptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.*;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

@Configuration
@CrossOrigin
@EnableWebMvc
@EnableAsync
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";

    @Autowired
    LogInterceptor logInterceptor;


    @Override
    public void addCorsMappings(CorsRegistry registry) { // cấu hình Cors
        registry.addMapping("/**").allowCredentials(true).allowedMethods("*").allowedOrigins("*").allowedHeaders("*");
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) { // cấu hình Interceptors
        String[] exclusive = {"/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/**", "/swagger-ui.html", "/webjars/**"};
        registry.addInterceptor(logInterceptor).addPathPatterns("/**").excludePathPatterns(exclusive);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        builder.serializationInclusion(JsonInclude.Include.NON_EMPTY);
        builder.dateFormat(new SimpleDateFormat(DATE_TIME_FORMAT));
        builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        builder.indentOutput(true);
        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
        converters.add(new MappingJackson2XmlHttpMessageConverter(builder.createXmlMapper(true).build()));
        converters.add(new ResourceHttpMessageConverter());
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) { // định nghĩa resource xử lí các tài nguyên tĩnh như hình ảnh ....
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        final UrlBasedCorsConfigurationSource source = getConfig();
        return new FilterRegistrationBean<>(new CorsFilter(source));
    }

    @Bean
    public CorsFilter corsFilterSecurity() {
        final UrlBasedCorsConfigurationSource source = getConfig();
        return new CorsFilter(source);
    }

    private UrlBasedCorsConfigurationSource getConfig() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedHeaders(Arrays.asList(
                "Accept", "Origin", "Content-Type", "Depth", "User-Agent", "If-Modified-Since,",
                "Cache-Control", "Authorization", "X-Req", "X-File-Size", "X-Requested-With", "X-File-Name","Content-Disposition"));//Arrays.asList("Origin", "Content-Type", "Accept")
        config.setAllowedMethods(Arrays.asList("HEAD","GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
        config.setExposedHeaders(Arrays.asList("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization","Content-Disposition"));
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateFormatter dateFormatter = new DateFormatter(DATE_TIME_FORMAT);
        dateFormatter.setLenient(true);
        registry.addFormatter(dateFormatter);
    }


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleDateFormat format = new SimpleDateFormat(WebMvcConfig.DATE_TIME_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(format);
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        return objectMapper;
    }
}
