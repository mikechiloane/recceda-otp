package com.recceda.core.store.github;

import com.recceda.action.RepositoryAction;
import com.recceda.core.store.OtpStore;
import com.recceda.core.store.reccedda.ReccedaOtpStore;
import com.recceda.http.github.GithubClient;

public class GithubOtpStore implements OtpStore {

    private final GithubClient githubClient;
    private final RepositoryAction repositoryAction;

    public GithubOtpStore(String githubToken) {
        this.githubClient = new GithubClient(githubToken);
        this.repositoryAction = new RepositoryAction(this.githubClient);
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

    public static class OtpEntry {
        private String otp;
        private String key;
        public long expiryTime;
        public int failedAttempts;    }
}
