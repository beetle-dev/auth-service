package com.cafe.authservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    ADMIN("관리자", 4),
    MANAGER("점장", 3),
    STAFF("스태프", 2),
    PENDING("대기", 1);

    private final String roleName;
    private final int level;
}
