package org.elcer.accounts.model;

import lombok.*;

import java.util.List;


@Value
@Builder
public class AccountListResponse {
    List<Account> accounts;
}
