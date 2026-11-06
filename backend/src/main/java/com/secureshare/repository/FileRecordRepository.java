package com.secureshare.repository;
import com.secureshare.entity.FileRecord;
import com.secureshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord,Long> {
    List<FileRecord> findByOwnerAndStatus(User owner, FileRecord.FileStatus status);
    @Query("SELECT f FROM FileRecord f WHERE f.status='ACTIVE' AND f.expiresAt IS NOT NULL AND f.expiresAt < :now")
    List<FileRecord> findExpiredFiles(LocalDateTime now);
    @Query("SELECT f FROM FileRecord f WHERE f.status='ACTIVE' AND f.maxDownloads IS NOT NULL AND f.downloadCount >= f.maxDownloads")
    List<FileRecord> findExhaustedFiles();
}
