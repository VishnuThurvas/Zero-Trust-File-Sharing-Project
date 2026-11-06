package com.secureshare.repository;
import com.secureshare.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    List<AuditLog> findByActorUsernameOrderByTimestampDesc(String username);
    Optional<AuditLog> findTopByOrderByIdDesc();
    @Query("SELECT a FROM AuditLog a ORDER BY a.id DESC") List<AuditLog> findAllOrderByIdDesc();
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.actorUsername=:username AND a.action='USER_LOGIN_FAILED' AND a.timestamp>:since")
    long countFailedLoginsAfter(String username, LocalDateTime since);
    List<AuditLog> findByTimestampBefore(LocalDateTime before);
    @Query("SELECT a FROM AuditLog a WHERE a.action='HONEY_FILE_ACCESSED' ORDER BY a.timestamp DESC")
    List<AuditLog> findHoneyFileAccesses();
}
