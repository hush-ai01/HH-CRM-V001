package com.highlands.highlandscrmbackend.role;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.highlands.highlandscrmbackend.common.exception.RoleAlreadyExistsException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;

    public RoleService(
            RoleRepository roleRepository,
            CompanyRepository companyRepository
    ) {
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
    }

    public RoleResponse createRole(RoleCreateRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company with id '" + request.companyId() + "' not found"
                ));

        if (roleRepository.existsByCompanyIdAndName(
                request.companyId(),
                request.name()
        )) {
            throw new RoleAlreadyExistsException(
                    "Role with name '" + request.name() +
                            "' already exists for this company"
            );
        }

        Role role = new Role(
                company,
                request.name(),
                request.description()
        );

        Role savedRole = roleRepository.save(role);

        return RoleResponse.from(savedRole);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByCompany(UUID companyId) {

        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException(
                    "Company with id '" + companyId + "' not found"
            );
        }

        return roleRepository.findAllByCompanyId(companyId)
                .stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role with id '" + id + "' not found"
                ));

        return RoleResponse.from(role);
    }
}