package com.highlands.highlandscrmbackend.company;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.highlands.highlandscrmbackend.common.exception.CompanyAlreadyExistsException;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public CompanyResponse createCompany(CompanyCreateRequest request) {

        if (companyRepository.existsByCode(request.code())) {
            throw new CompanyAlreadyExistsException(
                    "Company with code '" + request.code() + "' already exists"
            );
        }

        Company company = new Company(
                request.name(),
                request.code()
        );

        Company savedCompany = companyRepository.save(company);

        return CompanyResponse.from(savedCompany);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {

        return companyRepository.findAll()
                .stream()
                .map(CompanyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(UUID id) {

        Company company = companyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company with id '" + id + "' not found"
                        )
                );

        return CompanyResponse.from(company);
    }
}