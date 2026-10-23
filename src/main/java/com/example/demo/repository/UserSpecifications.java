package com.example.demo.repository;

import com.example.demo.model.AppUser;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<AppUser> search(String search) {
        return (root, query, cb) ->
                (search == null || search.isBlank())
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("username")), "%" + search.trim().toLowerCase() + "%");
    }
}