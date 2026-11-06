package com.secureshare.service;
import com.secureshare.dto.*; import com.secureshare.entity.*;
import com.secureshare.repository.*; import com.secureshare.util.CryptoUtil; import com.secureshare.util.JwtUtil;
import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime; import java.util.List; import java.util.stream.Collectors;
@Service
public class FileService {
    private final FileRecordRepository fr; private final FileShareRepository sr; private final UserRepository ur;
    private final StorageService st; private final AuditLogService au; private final MalwareScanService ms;
    private final CryptoUtil cr; private final JwtUtil jw;
    public FileService(FileRecordRepository fr,FileShareRepository sr,UserRepository ur,StorageService st,AuditLogService au,MalwareScanService ms,CryptoUtil cr,JwtUtil jw){
        this.fr=fr;this.sr=sr;this.ur=ur;this.st=st;this.au=au;this.ms=ms;this.cr=cr;this.jw=jw;}
    @Transactional public FileUploadResponse upload(MultipartFile file,String uname,Integer maxDl,Integer expHrs,boolean honey,String ip,String encKey,String iv,String hash) throws Exception {
        User owner=ur.findByUsername(uname).orElseThrow(()->new RuntimeException("User not found"));
        byte[] b=file.getBytes(); String key=st.store(b,file.getOriginalFilename());
        LocalDateTime exp=expHrs!=null?LocalDateTime.now().plusHours(expHrs):null;
        FileRecord rec=FileRecord.builder().originalFilename(file.getOriginalFilename()).storageKey(key)
            .fileSize(b.length).mimeType(file.getContentType()).sha256Hash(hash).encryptedAesKey(encKey)
            .aesIv(iv).owner(owner).maxDownloads(maxDl).expiresAt(exp).honeyFile(honey).scanStatus(FileRecord.ScanStatus.PENDING).build();
        fr.save(rec); ms.scanAsync(rec,b);
        au.log(AuditLog.AuditAction.FILE_UPLOADED,uname,rec.getId(),file.getOriginalFilename(),null,ip,null,"Size:"+b.length+"|Honey:"+honey);
        return FileUploadResponse.builder().fileId(rec.getId()).filename(file.getOriginalFilename())
            .sha256Hash(hash).encryptedAesKey(encKey).aesIv(iv).expiresAt(exp).maxDownloads(maxDl).message("Uploaded").build();}
    @Transactional public FileDownloadResponse download(Long fid,String uname,String ip) throws Exception {
        FileRecord rec=fr.findById(fid).orElseThrow(()->new RuntimeException("File not found"));
        User user=ur.findByUsername(uname).orElseThrow(()->new RuntimeException("User not found"));
        boolean own=rec.getOwner().getId().equals(user.getId());
        boolean share=sr.findByFileAndSharedWith(rec,user).map(s->s.isActive()&&(s.getExpiresAt()==null||s.getExpiresAt().isAfter(LocalDateTime.now()))).orElse(false);
        if(!own&&!share) throw new SecurityException("Access denied");
        if(rec.isHoneyFile()&&!own){au.log(AuditLog.AuditAction.HONEY_FILE_ACCESSED,uname,fid,rec.getOriginalFilename(),null,ip,null,"HONEY ACCESSED");throw new SecurityException("Access denied");}
        if(rec.getStatus()!=FileRecord.FileStatus.ACTIVE) throw new RuntimeException("File unavailable");
        if(rec.getExpiresAt()!=null&&rec.getExpiresAt().isBefore(LocalDateTime.now())){rec.setStatus(FileRecord.FileStatus.EXPIRED);fr.save(rec);throw new RuntimeException("Expired");}
        if(rec.getMaxDownloads()!=null&&rec.getDownloadCount()>=rec.getMaxDownloads()){rec.setStatus(FileRecord.FileStatus.EXPIRED);fr.save(rec);throw new RuntimeException("Limit reached");}
        byte[] data=st.retrieve(rec.getStorageKey());
        String ek=own?rec.getEncryptedAesKey():sr.findByFileAndSharedWith(rec,user).map(FileShare::getRecipientEncryptedKey).orElse(rec.getEncryptedAesKey());
        rec.setDownloadCount(rec.getDownloadCount()+1);fr.save(rec);
        au.log(AuditLog.AuditAction.FILE_DOWNLOADED,uname,fid,rec.getOriginalFilename(),null,ip,null,"DL#"+rec.getDownloadCount());
        return FileDownloadResponse.builder().filename(rec.getOriginalFilename()).encryptedData(cr.toBase64(data))
            .encryptedAesKey(ek).aesIv(rec.getAesIv()).sha256Hash(rec.getSha256Hash()).mimeType(rec.getMimeType()).build();}
    @Transactional public ShareResponse share(Long fid,String own,String tgt,FileShare.Permission perm,Integer hrs,String ip,String recipientEncryptedAesKey){
        FileRecord rec=fr.findById(fid).orElseThrow(()->new RuntimeException("File not found"));
        User owner=ur.findByUsername(own).orElseThrow(()->new RuntimeException("Owner not found"));
        User target=ur.findByUsername(tgt).orElseThrow(()->new RuntimeException("User not found: "+tgt));
        if(!rec.getOwner().getId().equals(owner.getId())) throw new SecurityException("Only owner can share");
        String tok=jw.generateShareToken(own,fid,hrs!=null?hrs:24);
        // recipientEncryptedAesKey = AES key re-encrypted with RECIPIENT's RSA public key (done in browser)
        // This means only the recipient's private key can decrypt it — true Zero-Knowledge sharing
        FileShare s=FileShare.builder().file(rec).sharedWith(target).shareToken(tok).permission(perm)
            .expiresAt(hrs!=null?LocalDateTime.now().plusHours(hrs):null).recipientEncryptedKey(recipientEncryptedAesKey).build();
        sr.save(s); au.log(AuditLog.AuditAction.FILE_SHARED,own,fid,rec.getOriginalFilename(),tgt,ip,null,"Perm:"+perm);
        return ShareResponse.builder().shareToken(tok).sharedWith(tgt).permission(perm.name()).expiresAt(s.getExpiresAt()).message("Shared with "+tgt).build();}
    @Transactional public void delete(Long fid,String uname,String ip) throws Exception {
        FileRecord rec=fr.findById(fid).orElseThrow(()->new RuntimeException("File not found"));
        User u=ur.findByUsername(uname).orElseThrow(()->new RuntimeException("User not found"));
        if(!rec.getOwner().getId().equals(u.getId())) throw new SecurityException("Only owner can delete");
        st.delete(rec.getStorageKey()); rec.setStatus(FileRecord.FileStatus.DELETED); fr.save(rec);
        au.log(AuditLog.AuditAction.FILE_DELETED,uname,fid,rec.getOriginalFilename(),null,ip,null,"Deleted");}
    public List<FileInfoResponse> listMyFiles(String uname){
        User u=ur.findByUsername(uname).orElseThrow(()->new RuntimeException("User not found"));
        return fr.findByOwnerAndStatus(u,FileRecord.FileStatus.ACTIVE).stream().map(this::info).collect(Collectors.toList());}
    public List<FileInfoResponse> listSharedWithMe(String uname){
        User u=ur.findByUsername(uname).orElseThrow(()->new RuntimeException("User not found"));
        return sr.findBySharedWithAndActive(u,true).stream().filter(s->s.getFile().getStatus()==FileRecord.FileStatus.ACTIVE).map(s->info(s.getFile())).collect(Collectors.toList());}
    private FileInfoResponse info(FileRecord f){
        return FileInfoResponse.builder().id(f.getId()).filename(f.getOriginalFilename()).fileSize(f.getFileSize())
            .mimeType(f.getMimeType()).uploadedAt(f.getUploadedAt()).expiresAt(f.getExpiresAt())
            .downloadCount(f.getDownloadCount()).maxDownloads(f.getMaxDownloads())
            .scanStatus(f.getScanStatus().name()).honeyFile(f.isHoneyFile()).sha256Hash(f.getSha256Hash()).build();}
    @Scheduled(fixedRate=60000) @Transactional
    public void expire(){
        fr.findExpiredFiles(LocalDateTime.now()).forEach(f->{f.setStatus(FileRecord.FileStatus.EXPIRED);fr.save(f);});
        fr.findExhaustedFiles().forEach(f->{f.setStatus(FileRecord.FileStatus.EXPIRED);fr.save(f);});}
}
