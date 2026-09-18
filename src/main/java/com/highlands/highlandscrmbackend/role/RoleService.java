package com.highlands.highlandscrmbackend.role;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.common.exception.RoleAlreadyExistsException;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        UUID companyId = TenantContext.requireCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company with id '" + companyId + "' not found"
                ));

        if (roleRepository.existsByCompanyIdAndName(
                companyId,
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
    public List<RoleResponse> getRolesByCompany() {

        UUID companyId = TenantContext.requireCompanyId();

        return roleRepository.findAllByCompanyId(companyId)
                .stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {

        UUID companyId = TenantContext.requireCompanyId();

        Role role = roleRepository.findByIdAndCompanyId(
                        id,
                        companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role with id '" + id + "' not found"
                ));

        return RoleResponse.from(role);
    }
}