package vn.iostar.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {
    String storeFile(MultipartFile file) throws IOException;
}