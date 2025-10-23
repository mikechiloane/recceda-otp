package com.recceda.core.store.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.recceda.action.FileAction;
import com.recceda.action.RepositoryAction;
import com.recceda.core.store.OtpStore;
import com.recceda.core.store.reccedda.ReccedaOtpStore;
import com.recceda.elements.Committer;
import com.recceda.elements.Repository;
import com.recceda.http.github.GithubClient;
import com.recceda.http.requests.file.CreateFileRequest;
import com.recceda.http.requests.repository.CreateRepositoryRequest;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

public class GithubOtpStore implements OtpStore {

    private final GithubClient githubClient;
    private final RepositoryAction repositoryAction;
    private final FileAction fileAction;
    private final Repository repository;
    private final String otpStoreName;
    private final Committer committer;

    public GithubOtpStore(String githubToken, String otpStoreName) throws ExecutionException, InterruptedException, JsonProcessingException {
        this.otpStoreName = otpStoreName;
        this.githubClient = new GithubClient(githubToken);
        this.repositoryAction = new RepositoryAction(this.githubClient);
        this.fileAction = new FileAction(this.githubClient);
        this.repository = this.repositoryAction.createRepositoryForAuthenticatedUser(this.createOtpRepository(otpStoreName));
        this.committer = Committer.builder().email(repository.getOwner().getLogin() + "@example.com").name(this.getClass().getName()).build();

    }

    @Override
    public void storeOtp(String key, String otp, long ttlMillis) {
        OtpEntry otpEntry = new OtpEntry(key, otp);
        try {
            CreateFileRequest createFileRequest = new CreateFileRequest(otpEntry, committer, LocalDate.now().toString() + " " + this.otpStoreName + " " + key);

            fileAction.createFile(createFileRequest, this.repository, "otp" + key + ".json");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private CreateFileRequest createFileRequestForOtp(OtpEntry otpEntry) throws JsonProcessingException {
        String message = LocalDate.now().toString() + " " + this.otpStoreName + " " + otpEntry.key;
        Committer committer = Committer.builder()
                .name(this.getClass().getName())
                .email("test@example.com").build();

        return new CreateFileRequest(otpEntry, committer, message);
    }


    @AllArgsConstructor
    public static class OtpEntry {
        private String key;
        private String otp;

    }
}
