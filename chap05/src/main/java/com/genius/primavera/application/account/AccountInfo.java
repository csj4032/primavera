package com.genius.primavera.application.account;

import java.time.LocalDate;

public record AccountInfo(LocalDate date, long amount, Category category) {

}
