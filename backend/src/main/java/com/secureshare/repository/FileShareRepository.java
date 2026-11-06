package com.secureshare.repository;
import com.secureshare.entity.FileRecord;
import com.secureshare.entity.FileShare;
import com.secureshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface FileShareRepository extends JpaRepository<FileShare,Long> {
    List<FileShare> findBySharedWithAndActive(User sharedWith, boolean active);
    Optional<FileShare> findByFileAndSharedWith(FileRecord file, User sharedWith);
}
