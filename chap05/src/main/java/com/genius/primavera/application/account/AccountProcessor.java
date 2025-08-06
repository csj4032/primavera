package com.genius.primavera.application.account;

import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

public record AccountProcessor(List<AccountInfo> accountInfos) {

    public long calculationTotalAmount() {
        return accountInfos.stream().map(e -> e.amount()).collect(Collectors.summingLong(Long::longValue));
    }

    public long calculationTotalForCategory(Category category) {
        return accountInfos.stream().filter(e -> e.category().equals(category)).map(e -> e.amount()).reduce(0l, Long::sum);
    }

    public long calculationTotalForMonth(Month month) {
        return accountInfos.stream().filter(e -> e.date().getMonth().equals(month)).map(e -> e.amount()).reduce(0l, (a, b) -> a + b);
    }
}