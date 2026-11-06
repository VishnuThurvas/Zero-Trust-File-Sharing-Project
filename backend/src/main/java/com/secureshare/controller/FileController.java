package com.secureshare.controller;

import com.secureshare.dto.*;
import com.secureshare.entity.FileShare;
import com.secureshare.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam String encryptedAesKey,
            @RequestParam String aesIv,
            @RequestParam String sha256Hash,
            @RequestParam(required = false) Integer maxDownloads,
            @RequestParam(required = false) Integer expiryHours,
            @RequestParam(defaultValue = "false") boolean honeyFile,
            Authentication auth, HttpServletRequest req) throws Exception {
        return ResponseEntity.ok(fileService.upload(file, auth.getName(), maxDownloads,
                expiryHours, honeyFile, req.getRemoteAddr(), encryptedAesKey, aesIv, sha256Hash));
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<FileDownloadResponse> download(@PathVariable Long fileId,
            Authentication auth, HttpServletRequest req) throws Exception {
        return ResponseEntity.ok(fileService.download(fileId, auth.getName(), req.getRemoteAddr()));
    }

    @PostMapping("/share/{fileId}")
    public ResponseEntity<ShareResponse> share(
            @PathVariable Long fileId,
            @RequestParam String targetUsername,
            @RequestParam(defaultValue = "VIEWER") String permission,
            @RequestParam(required = false) Integer expiryHours,
            // The AES key re-encrypted with the RECIPIENT's public key — done in browser
            @RequestParam String recipientEncryptedAesKey,
            Authentication auth, HttpServletRequest req) throws Exception {
        return ResponseEntity.ok(fileService.share(fileId, auth.getName(), targetUsername,
                FileShare.Permission.valueOf(permission), expiryHours,
                req.getRemoteAddr(), recipientEncryptedAesKey));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> delete(@PathVariable Long fileId,
            Authentication auth, HttpServletRequest req) throws Exception {
        fileService.delete(fileId, auth.getName(), req.getRemoteAddr());
        return ResponseEntity.ok("File deleted");
    }

    @GetMapping("/my-files")
    public ResponseEntity<List<FileInfoResponse>> myFiles(Authentication auth) {
        return ResponseEntity.ok(fileService.listMyFiles(auth.getName()));
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<List<FileInfoResponse>> sharedWithMe(Authentication auth) {
        return ResponseEntity.ok(fileService.listSharedWithMe(auth.getName()));
    }
}
