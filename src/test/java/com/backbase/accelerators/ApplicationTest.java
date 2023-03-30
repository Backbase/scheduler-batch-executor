package com.backbase.accelerators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
 class ApplicationTest {

    @BeforeAll
    public static void envSetup() {
        System.setProperty("SIG_SECRET_KEY", "JWTSecretKeyDontUseInProduction!");
        System.setProperty("server.port", "0");
    }
    @Test
    void main() {
        Assertions.assertDoesNotThrow(() -> Application.main(new String[]{}));
    }
}