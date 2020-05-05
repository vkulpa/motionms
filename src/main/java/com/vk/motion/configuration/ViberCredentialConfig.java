package com.vk.motion.configuration;

import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponseSupport;

@Configuration
@Getter
@RequiredArgsConstructor
public class ViberCredentialConfig {

  private final VaultOperations vaultOperations;
  private static final String SECRET_PATH = "cubbyhole/motionms/viber";

  private String token;
  private String adminProfileId;

  @PostConstruct
  public void init() {
    VaultResponseSupport<VaultViberResponse> vaultResponse =
        vaultOperations.read(SECRET_PATH, VaultViberResponse.class);

    Optional.ofNullable(vaultResponse)
        .orElseThrow(() -> new IllegalStateException("Vault response missing"));
    Optional.ofNullable(vaultResponse.getData())
        .orElseThrow(() -> new IllegalStateException("Vault response missing data"));

    token = vaultResponse.getData().getToken();
    adminProfileId = vaultResponse.getData().getAdminProfileId();
  }

  @Value
  private static class VaultViberResponse {

    private String token;
    private String adminProfileId;
  }
}
