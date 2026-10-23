package com.example.demo.config;

import com.example.demo.model.Authority;
import com.example.demo.repository.AuthorityRepository;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PreAuthorizeAuthorityScanner implements CommandLineRunner {

    @Autowired private ApplicationContext applicationContext;

    @Autowired private AuthorityRepository authorityRepository;

    private static final Pattern QUOTED = Pattern.compile("'([^']+)'");

    @Override
    public void run(String... args) {
        Set<String> discovered = scanControllers();

        int added = 0;
        for (String name : discovered) {
            if (!authorityRepository.existsById(name)) {
                Authority a = new Authority();
                a.setName(name);
                authorityRepository.save(a);
                added++;
            }
        }
        System.out.println("[PreAuthorizeAuthorityScanner] Scanned all @RestController classes, found "
                + discovered.size() + " authority names referenced in @PreAuthorize, added " + added + " new row(s).");
    }

    private Set<String> scanControllers() {
        Set<String> authorities = new LinkedHashSet<>();
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);

        for (Object bean : controllers.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);

            PreAuthorize classLevel = targetClass.getAnnotation(PreAuthorize.class);
            if (classLevel != null) {
                extractInto(classLevel.value(), authorities);
            }

            for (Method method : targetClass.getMethods()) {
                PreAuthorize methodLevel = method.getAnnotation(PreAuthorize.class);
                if (methodLevel != null) {
                    extractInto(methodLevel.value(), authorities);
                }
            }
        }
        return authorities;
    }

    private void extractInto(String expression, Set<String> out) {
        if (expression == null) return;
        extractCalls(expression, "hasAuthority", out, false);
        extractCalls(expression, "hasAnyAuthority", out, false);
        extractCalls(expression, "hasRole", out, true);
        extractCalls(expression, "hasAnyRole", out, true);
    }

    private void extractCalls(String expression, String functionName, Set<String> out, boolean prefixRole) {
        int idx = 0;
        while ((idx = expression.indexOf(functionName + "(", idx)) != -1) {
            int start = idx + functionName.length() + 1;
            int end = expression.indexOf(')', start);
            if (end == -1) break;

            Matcher m = QUOTED.matcher(expression.substring(start, end));
            while (m.find()) {
                String value = m.group(1);
                out.add(prefixRole ? "ROLE_" + value : value);
            }
            idx = end;
        }
    }
}