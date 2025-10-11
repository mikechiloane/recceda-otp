package com.recceda.core.store.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.recceda.action.RepositoryAction;
import com.recceda.core.store.OtpStore;
import com.recceda.core.store.reccedda.ReccedaOtpStore;
import com.recceda.elements.Repository;
import com.recceda.http.github.GithubClient;
import com.recceda.http.requests.repository.CreateRepositoryRequest;

import java.util.concurrent.ExecutionException;

public class GithubOtpStore implements OtpStore {

    private final GithubClient githubClient;
    private final RepositoryAction repositoryAction;
    private final Repository repository;
    private final String otpStoreName;

    public GithubOtpStore(String githubToken, String otpStoreName) throws ExecutionException, InterruptedException, JsonProcessingException {
        this.otpStoreName = otpStoreName;
        this.githubClient = new GithubClient(githubToken);
        this.repositoryAction = new RepositoryAction(this.githubClient);
        this.repository = this.repositoryAction.createRepositoryForAuthenticatedUser(this.createOtpRepository(otpStoreName));
    }

    @Override
    public void storeOtp(String key, String otp, long ttlMillis) {

    }

    @Override
    public boolean verifyOtp(String key, String otp) {
        return false;
    }

    @Override
    public ReccedaOtpStore.OtpEntry getOtpEntry(String key) {
        return null;
    }

    @Override
    public void invalidateOtp(String key) {

    }

    private CreateRepositoryRequest createOtpRepository(String otpStoreName) {
        return CreateRepositoryRequest.builder()
                .description("Recceda Otp Repository")
                .name(this.otpStoreName)
                .isPrivate(false)
                .isTemplate(false)
                .build();
    }

    public static class OtpEntry {
        public long expiryTime;
        public int failedAttempts;
        private String otp;
        private String key;
    }
}
