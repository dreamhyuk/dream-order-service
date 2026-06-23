package com.dreamhyuk.dream_order.domain.member;

public enum MemberRole {
    CUSTOMER, OWNER, RIDER;

    /**
     * 🌟 Redis Key 조립을 위한 순수 문자열 반환 (ROLE_ 접두사 방어)
     */
    public String toRedisPrefix() {
        String roleStr = this.name();
        if (roleStr.startsWith("ROLE_")) {
            roleStr = roleStr.replace("ROLE_", "");
        }
        return roleStr.toUpperCase();
    }
}
