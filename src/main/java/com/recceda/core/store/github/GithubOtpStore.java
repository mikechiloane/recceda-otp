package com.recceda.core.store.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.recceda.OtpEntry;
import com.recceda.action.FileAction;
import com.recceda.action.RepositoryAction;
import com.recceda.core.store.OtpStore;
import com.recceda.core.util.HashingUtil;
import com.recceda.elements.Committer;
import com.recceda.elements.Repository;
import com.recceda.http.github.GithubClient;
import com.recceda.http.requests.file.CreateFileRequest;
import com.recceda.http.requests.file.DeleteFileRequest;
import com.recceda.http.requests.repository.CreateRepositoryRequest;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

public class GithubOtpStore implements OtpStore {

    private final RepositoryAction repositoryAction;
    private final FileAction fileAction;
    private final Repository repository;
    private final String otpStoreName;

    public GithubOtpStore(String githubToken, String otpStoreName) throws ExecutionException, InterruptedException, JsonProcessingException {
        this.otpStoreName = otpStoreName;
        GithubClient githubClient = new GithubClient(githubToken);
        this.repositoryAction = new RepositoryAction(githubClient);
        this.fileAction = new FileAction(githubClient);

        this.repository = this.repositoryAction.createRepositoryForAuthenticatedUser(this.createOtpRepository(otpStoreName));

    }

    @Override
    public void storeOtp(String key, String plainOtp, long ttlMillis) {
        OtpEntry otp = OtpEntry.builder().otpHash(HashingUtil.hashOtp(plainOtp)).expiryTime(System.currentTimeMillis() + ttlMillis).failedAttempts(0).key(key).build();
        try {
            fileAction.createFile(this.createFileRequestForOtp(otp), this.repository, this.generateOtpFileName(key));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verifyOtp(String key, String plainOtp) {
        try {
            OtpEntry otp = fileAction.getFileContents(this.repository.getOwner().getLogin(), this.repository.getName(), this.
                    generateOtpFileName(key), OtpEntry.class);

            String otpHash = HashingUtil.hashOtp(plainOtp);
            String otpHashFromStore = otp.getOtpHash();
            if (!otpHash.equals(otpHashFromStore)) {
                String sha = fileAction.getFileContents(this.repository.getOwner().getLogin(), this.repository.getName(), this.generateOtpFileName(key)).getSha();
                otp.setFailedAttempts(otp.getFailedAttempts() + 1);
                CreateFileRequest newUpdateReqeust = this.createFileRequestForOtp(otp);
                newUpdateReqeust.setSha(sha);
                fileAction.updateFile(newUpdateReqeust, repository.getOwner().getLogin(), repository.getName(), this.generateOtpFileName(key));
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public OtpEntry getOtpEntry(String key) {
        try {
            return fileAction.getFileContents(this.repository.getOwner().getLogin(), this.repository.getName(), this.generateOtpFileName(key), OtpEntry.class);
        } catch (Exception e) {
            return null;
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
        String message = LocalDate.now() + " " + this.otpStoreName + " " + otpEntry.getKey();
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

    public void destroy() throws ExecutionException, InterruptedException {
        repositoryAction.deleteRepositoryForAuthenticatedUser(this.repository.getOwner().getLogin(), this.repository.getName());
    }


}
