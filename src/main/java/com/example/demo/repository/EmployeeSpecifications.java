package com.example.demo.repository;

import com.example.demo.dto.EmployeeSearchCriteria;
import com.example.demo.model.Employee;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecifications {

    public static Specification<Employee> search(EmployeeSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String like = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                var deptJoin = root.join("department", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(deptJoin.get("departmentName")), like)
                ));
            }
            if (criteria.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("department").get("deptId"), criteria.getDepartmentId()));
            }
            if (criteria.getMinSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), criteria.getMinSalary()));
            }
            if (criteria.getMaxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), criteria.getMaxSalary()));
            }

            if (criteria.getGrade() != null) {
                predicates.add(cb.equal(root.get("grade"), criteria.getGrade()));
            }
            if (Boolean.TRUE.equals(criteria.getSupervisorOnly())) {
                predicates.add(cb.isTrue(root.get("supervisor")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}