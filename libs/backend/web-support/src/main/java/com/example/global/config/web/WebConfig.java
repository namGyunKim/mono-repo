package com.example.global.config.web;

import com.example.global.resolver.CurrentAccountResolver;
import com.example.global.version.ApiVersioning;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceChainRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private static final String WEB_JARS_RESOLVER_CLASS_NAME = "org.springframework.web.servlet.resource.WebJarsResourceResolver";

    private final CurrentAccountResolver currentAccountResolver;
    private final Environment environment;

    /**
     * Spring Boot 4 / Spring Framework 7: First-class API Versioning
     *
     * <p>
     * - 요청 헤더(API-Version)로 버전을 식별합니다.
     * - 버전이 없으면 DEFAULT_VERSION(0.0)으로 처리합니다. (의도된 정책)
     * </p>
     */
    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useRequestHeader(ApiVersioning.HEADER_NAME)
                .setDefaultVersion(ApiVersioning.DEFAULT_VERSION);
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        // [중요] templates 디렉토리를 정적 리소스로 노출하면 서버 사이드 템플릿 원본이 외부에 그대로 노출될 수 있습니다.
        // 정적 리소스는 /static 및 webjars 만 노출합니다.

        final boolean cacheResources = isResourceChainCacheEnabled();

        final CacheControl longCacheControl = cacheResources
                ? CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic()
                : CacheControl.noStore();

        // Service Worker는 브라우저 업데이트 정책상 "항상 최신 체크"가 중요할 수 있으므로,
        // 장기 캐시를 적용하지 않는다.
        registry.addResourceHandler("/sw.js")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.noCache())
                .resourceChain(cacheResources)
                .addResolver(new PathResourceResolver());

        final ResourceChainRegistration webJarsChain = registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCacheControl(longCacheControl)
                .resourceChain(cacheResources);

        // Spring 버전에 따라 WebJarsResourceResolver 클래스가 없을 수 있습니다.
        // - 존재하면: 버전 없는 경로(/webjars/jquery/jquery.min.js)로도 접근 가능
        // - 없으면: 버전 포함 경로(/webjars/jquery/3.7.1/jquery.min.js)만 동작
        addWebJarsResourceResolverIfPresent(webJarsChain);

        webJarsChain.addResolver(new PathResourceResolver());

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(longCacheControl)
                // 정적 리소스 내용(Content) 기반 해시 버전 적용
                // 예: /css/output.css -> /css/output-{hash}.css
                .resourceChain(cacheResources)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"))
                // CSS 내부 url(...) 참조까지 버전이 적용되도록 변환
                .addTransformer(new CssLinkResourceTransformer())
                .addResolver(new PathResourceResolver());
    }

    private void addWebJarsResourceResolverIfPresent(ResourceChainRegistration chain) {
        try {
            final Class<?> clazz = Class.forName(WEB_JARS_RESOLVER_CLASS_NAME);
            final Object instance = clazz.getDeclaredConstructor().newInstance();

            if (instance instanceof ResourceResolver resolver) {
                chain.addResolver(resolver);
            }
        } catch (ClassNotFoundException e) {
            // 현재 Spring 버전에는 WebJarsResourceResolver가 없습니다. (무시)
        } catch (ReflectiveOperationException e) {
            // 리플렉션 실패 시 정적 리소스 제공 자체가 깨지는 것을 막기 위해, resolver 추가를 스킵합니다. (무시)
        }
    }

    private boolean isResourceChainCacheEnabled() {
        // local 프로필에서는 Tailwind 등 정적 파일이 자주 바뀌므로,
        // 버전 매핑/해시 결과를 캐시하지 않아도 즉시 반영되도록 cache=false로 운영한다.
        // prod 등에서는 성능을 위해 cache=true를 사용한다.
        for (final String profile : environment.getActiveProfiles()) {
            if ("local".equalsIgnoreCase(profile)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(currentAccountResolver);
    }
}
