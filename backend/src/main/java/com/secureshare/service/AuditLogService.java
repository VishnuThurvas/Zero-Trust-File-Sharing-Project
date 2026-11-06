package com.secureshare.service;
import com.secureshare.entity.AuditLog; import com.secureshare.repository.AuditLogRepository;
import com.secureshare.util.CryptoUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime; import java.util.List;
@Service
public class AuditLogService {
    private final AuditLogRepository repo; private final CryptoUtil crypto;
    public AuditLogService(AuditLogRepository repo,CryptoUtil crypto){this.repo=repo;this.crypto=crypto;}
    @Transactional public AuditLog log(AuditLog.AuditAction action,String actor,Long fileId,String filename,String target,String ip,String ua,String details){
        try{String prev=repo.findTopByOrderByIdDesc().map(AuditLog::getRecordHash).orElse(null);
            String data=action+"|"+actor+"|"+fileId+"|"+filename+"|"+ip+"|"+LocalDateTime.now();
            String hash=crypto.computeLogHash(prev,data);
            AuditLog e=AuditLog.builder().action(action).actorUsername(actor!=null?actor:"SYSTEM")
                .fileId(fileId).filename(filename).targetUsername(target).ipAddress(ip).userAgent(ua)
                .details(details).previousHash(prev).recordHash(hash).build();
            return repo.save(e);}
        catch(Exception e){throw new RuntimeException("Audit failed: "+e.getMessage(),e);}}
    public List<AuditLog> getUserLogs(String u){return repo.findByActorUsernameOrderByTimestampDesc(u);}
    public List<AuditLog> getAllLogs(){return repo.findAllOrderByIdDesc();}
    public List<AuditLog> getHoneyAlerts(){return repo.findHoneyFileAccesses();}
    public boolean verifyChainIntegrity(){
        List<AuditLog> logs=repo.findAll(); String prev=null;
        for(AuditLog e:logs){try{
            String data=e.getAction()+"|"+e.getActorUsername()+"|"+e.getFileId()+"|"+e.getFilename()+"|"+e.getIpAddress()+"|"+e.getTimestamp();
            if(!crypto.computeLogHash(prev,data).equals(e.getRecordHash())) return false;
            prev=e.getRecordHash();}catch(Exception ex){return false;}} return true;}
    @Scheduled(cron="0 0 2 * * *") @Transactional
    public void clean(){List<AuditLog> old=repo.findByTimestampBefore(LocalDateTime.now().minusDays(90));repo.deleteAll(old);}
}
