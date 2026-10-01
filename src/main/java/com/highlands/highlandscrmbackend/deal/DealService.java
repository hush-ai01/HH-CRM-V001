package com.highlands.highlandscrmbackend.deal;

import com.highlands.highlandscrmbackend.client.Client;
import com.highlands.highlandscrmbackend.client.ClientRepository;
import com.highlands.highlandscrmbackend.company.Company;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.security.AuthorizationService;
import com.highlands.highlandscrmbackend.security.CurrentUserService;
import com.highlands.highlandscrmbackend.security.TenantContext;
import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.user.User;
import com.highlands.highlandscrmbackend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DealService {

    private final DealRepository dealRepository;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;

    public DealService(
            DealRepository dealRepository,
            CompanyRepository companyRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService
    ) {
        this.dealRepository = dealRepository;
        this.companyRepository = companyRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
    }

    public Deal create(
            UUID clientId,
            DealType type,
            String commodity,
            String grade,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            String currency,
            LocalDate expectedCloseDate,
            String notes
    ) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("DEAL_CREATE");

        UUID currentUserId = currentUserService.getCurrentUserId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found: " + companyId));

        Client client = clientRepository
                .findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client not found: " + clientId
                        ));

        User owner = userRepository
                .findByIdAndCompanyId(currentUserId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + currentUserId
                        ));

        String dealNumber = generateDealNumber(companyId);

        Deal deal = new Deal(
                company,
                client,
                owner,
                dealNumber,
                type,
                commodity,
                grade,
                quantity,
                unit,
                unitPrice,
                currency,
                expectedCloseDate,
                notes
        );

        return dealRepository.save(deal);
    }

    @Transactional(readOnly = true)
    public List<Deal> findAll() {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("DEAL_READ");

        return dealRepository
                .findAllByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    @Transactional(readOnly = true)
    public Deal findById(UUID dealId) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("DEAL_READ");

        return dealRepository
                .findByIdAndCompanyId(dealId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Deal not found: " + dealId
                        ));
    }

    public Deal update(
            UUID dealId,
            String commodity,
            String grade,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            String currency,
            LocalDate expectedCloseDate,
            String notes
    ) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("DEAL_UPDATE");

        Deal deal = dealRepository
                .findByIdAndCompanyId(dealId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Deal not found: " + dealId
                        ));

        deal.update(
                commodity,
                grade,
                quantity,
                unit,
                unitPrice,
                currency,
                expectedCloseDate,
                notes
        );

        return dealRepository.save(deal);
    }

    public Deal changeStatus(
            UUID dealId,
            DealStatus status
    ) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("DEAL_UPDATE");

        Deal deal = dealRepository
                .findByIdAndCompanyId(dealId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Deal not found: " + dealId
                        ));

        deal.changeStatus(status);

        return dealRepository.save(deal);
    }

    public void delete(UUID dealId) {
        UUID companyId = TenantContext.requireCompanyId();

        authorizationService.requirePermission("DEAL_DELETE");

        Deal deal = dealRepository
                .findByIdAndCompanyId(dealId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Deal not found: " + dealId
                        ));

        dealRepository.delete(deal);
    }

    private String generateDealNumber(UUID companyId) {
        String dealNumber;

        do {
            dealNumber = "DL-" +
                    java.time.Year.now().getValue() +
                    "-" +
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();
        } while (dealRepository.existsByCompanyIdAndDealNumber(
                companyId,
                dealNumber
        ));

        return dealNumber;
    }
}