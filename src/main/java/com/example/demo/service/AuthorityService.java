package com.example.demo.service;

import com.example.demo.dto.AuthorityDTO;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.model.Authority;
import com.example.demo.repository.AuthorityRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorityService {

    @Autowired private AuthorityRepository authorityRepository;

    public record CreateAuthorityRequest(
            @NotBlank(message = "Authority name is required") String name
    ) {}

    @Transactional
    public AuthorityDTO createAuthority(CreateAuthorityRequest request){
        String name = request.name().trim().toUpperCase().replace(' ', '_');
        if (authorityRepository.existsById(name)) {
            throw new IllegalArgumentException("Authority '" + name + "' already exists");
        }
        Authority authority = new Authority();
        authority.setName(name);

        return RoleMapper.toDto(authority);
    }

    public List<AuthorityDTO> getAllAuthorities(){
        return authorityRepository.findAll().stream().map(RoleMapper::toDto).toList();
    }
}
