package com.ecommerce.automation.support;

public record RegisterTestData(
        String testCaseId,
        String caseType,
        String scenario,
        String fullName,
        String email,
        String password,
        String expectedResult,
        String expectedMessage,
        boolean shouldRedirectToLogin,
        String notes
) {
}
