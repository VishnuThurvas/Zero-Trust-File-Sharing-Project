package com.secureshare.service;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*; import java.nio.file.*; import java.util.UUID;
@Service
public class StorageService {
    @Value("${app.upload.dir:./uploads}") private String dir;
    @PostConstruct public void init() throws IOException {
        Files.createDirectories(Paths.get(dir));
        System.out.println("[Storage] Dir: "+Paths.get(dir).toAbsolutePath());}
    public String store(byte[] data,String name) throws IOException {
        String ext=name.contains(".")?name.substring(name.lastIndexOf('.')):"";
        String key=UUID.randomUUID()+ext;
        Files.write(Paths.get(dir,key),data,StandardOpenOption.CREATE_NEW);
        System.out.println("[Storage] Stored: "+key+" ("+data.length+" bytes)"); return key;}
    public byte[] retrieve(String key) throws IOException {
        Path p=Paths.get(dir,key); if(!Files.exists(p)) throw new FileNotFoundException("Not found: "+key);
        return Files.readAllBytes(p);}
    public void delete(String key) throws IOException {Files.deleteIfExists(Paths.get(dir,key));}
}
