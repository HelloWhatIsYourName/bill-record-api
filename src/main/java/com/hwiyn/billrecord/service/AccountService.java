package com.hwiyn.billrecord.service;

import com.hwiyn.billrecord.dto.account.AccountResponse;
import com.hwiyn.billrecord.dto.account.CreateAccountRequest;
import com.hwiyn.billrecord.dto.account.UpdateAccountRequest;
import com.hwiyn.billrecord.entity.Account;
import com.hwiyn.billrecord.entity.Money;
import com.hwiyn.billrecord.exception.ApiException;
import com.hwiyn.billrecord.repository.AccountRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final Clock clock;

    public AccountService(AccountRepository accountRepository, Clock clock) {
        this.accountRepository = accountRepository;
        this.clock = clock;
    }

    @Transactional
    public AccountResponse create(UUID userId, CreateAccountRequest request) {
        String name = request.name().trim();
        if (accountRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new ApiException(HttpStatus.CONFLICT, "ACCOUNT_NAME_EXISTS", "Account name already exists");
        }
        Account account = Account.create(
                userId,
                name,
                request.type(),
                request.openingBalance(),
                request.currency() == null ? Money.DEFAULT_CURRENCY : request.currency(),
                Instant.now(clock));
        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listActive(UUID userId) {
        return accountRepository.findByUserIdAndArchivedFalseOrderByCreatedAtAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse get(UUID userId, UUID accountId) {
        return toResponse(requireAccount(userId, accountId));
    }

    @Transactional
    public AccountResponse update(UUID userId, UUID accountId, UpdateAccountRequest request) {
        Account account = requireAccount(userId, accountId);
        String requestedName = request.name();
        if (requestedName != null
                && !requestedName.isBlank()
                && !account.getName().equalsIgnoreCase(requestedName.trim())
                && accountRepository.existsByUserIdAndNameIgnoreCase(userId, requestedName.trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "ACCOUNT_NAME_EXISTS", "Account name already exists");
        }
        account.update(requestedName, request.type(), Instant.now(clock));
        return toResponse(account);
    }

    @Transactional
    public void archive(UUID userId, UUID accountId) {
        requireAccount(userId, accountId).archive(Instant.now(clock));
    }

    public Account requireAccount(UUID userId, UUID accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .filter(account -> !account.isArchived())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found"));
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getOpeningBalance(),
                account.getCurrentBalance(),
                account.getCurrency(),
                account.isArchived());
    }
}
