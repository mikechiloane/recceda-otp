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
import com.recceda.http.requests.file.DeleteFileRequest;
import com.recceda.http.requests.repository.CreateRepositoryRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

public class GithubOtpStore implements OtpStore {

    private final GithubClient githubClient;
    private final RepositoryAction repositoryAction;
    private final FileAction fileAction;
    private final Repository repository;
    private final String otpStoreName;

    public GithubOtpStore(String githubToken, String otpStoreName) throws ExecutionException, InterruptedException, JsonProcessingException {
        this.otpStoreName = otpStoreName;
        this.githubClient = new GithubClient(githubToken);
        this.repositoryAction = new RepositoryAction(this.githubClient);
        this.fileAction = new FileAction(this.githubClient);

        this.repository = this.repositoryAction.createRepositoryForAuthenticatedUser(this.createOtpRepository(otpStoreName));

    }

    @Override
    public void storeOtp(String key, String otp, long ttlMillis) {
        OtpEntry otpEntry = new OtpEntry(key, otp);
        try {
            fileAction.createFile(this.createFileRequestForOtp(otpEntry), this.repository, this.generateOtpFileName(key));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verifyOtp(String key, String otp) {
        try {
            OtpEntry otpEntry = fileAction.getFileContents(this.repository.getOwner().getLogin(), this.repository.getName(), this.
                    generateOtpFileName(key), OtpEntry.class);
            return otpEntry.otp.equals(otp);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ReccedaOtpStore.OtpEntry getOtpEntry(String key) {
        try {
            OtpEntry otpEntry = fileAction.getFileContents(this.repository.getOwner().getLogin(), this.repository.getName(), this.generateOtpFileName(key), OtpEntry.class);
            return new ReccedaOtpStore.OtpEntry(otpEntry.otp, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void invalidateOtp(String key) {
        try {
            String sha = fileAction.getFileContents(this.repository.getOwner().getLogin(), this.repository.getName(), this.generateOtpFileName(key)).getSha();
            fileAction.deleteFile(this.createDeleteFileRequest(key, sha), this.repository.getOwner().getLogin(), this.repository.getName(), this.generateOtpFileName(key));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        String message = LocalDate.now() + " " + this.otpStoreName + " " + otpEntry.key;
        Committer committer = Committer.builder()
                .name(this.getClass().getName())
                .email("test@example.com").build();

        return new CreateFileRequest(otpEntry, committer, message);
    }

    private DeleteFileRequest createDeleteFileRequest(String key, String sha) throws JsonProcessingException {
        String message = LocalDate.now() + " " + this.otpStoreName + " " + key
                + " deleted";
        Committer committer = Committer.builder()
                .name(this.getClass().getName())
                .email("test@email.com")
                .build();

        return DeleteFileRequest.builder().sha(sha).message(message).committer(committer).build();
    }

    private String generateOtpFileName(String key) {
        return "otp_" + key + ".json";
    }

    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class OtpEntry {
        private String key;
        private String otp;

    }

}
