package org.arkibo.services;

import com.backblaze.b2.client.*;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.structures.B2AccountAuthorization;
import com.backblaze.b2.client.structures.B2DownloadAuthorization;
import com.backblaze.b2.client.structures.B2GetDownloadAuthorizationRequest;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Instant;

public class StorageService {
    Dotenv dotenv = Dotenv.load();

    String keyId = dotenv.get("B2_KEY_ID");
    String key = dotenv.get("B2_KEY");
    String bucketId = dotenv.get("B2_BUCKET_ID");

    String authToken, apiUrl;

    private Instant tokenExpiry = Instant.EPOCH;

    final private B2StorageClient client = B2StorageClientFactory
            .createDefaultFactory()
            .create(keyId, key, "iskolar");

    final private String bucketName = "iskolar";

    private void ensureAuthentication() {
        try {
            if (Instant.now().isAfter(tokenExpiry)) {
                B2AccountAuthorization auth = client.getAccountAuthorization();
                this.authToken = auth.getAuthorizationToken();
                this.apiUrl = auth.getApiUrl();
            }
        } catch (B2Exception e) {
            throw new RuntimeException("Failed to authenticate with Backblaze", e);
        }
    }

    public String getAccessLink(String uuid) throws B2Exception {
        this.ensureAuthentication();
        B2GetDownloadAuthorizationRequest request = B2GetDownloadAuthorizationRequest.builder(
                this.bucketId,
                String.format("%s.pdf", uuid),
                3600
        ).build();

        B2DownloadAuthorization auth = this.client.getDownloadAuthorization(request);
        String authToken = auth.getAuthorizationToken();

        return String.format("%s/file/%s/%s.pdf?Authorization=%s", this.apiUrl, this.bucketName, uuid, authToken);
    }
}
