package com.wtmsbackend.services.serviceImp;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.wtmsbackend.exceptions.CloudinaryUploadException;
import com.wtmsbackend.services.CloudinaryImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryImageServiceImpl implements CloudinaryImageService {

    private final Cloudinary cloudinary;

    public CloudinaryImageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public Map<String, Object> upload(MultipartFile file) throws IOException {
        try {
            return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
        } catch (RuntimeException ex) {
            throw new CloudinaryUploadException("Invalid Cloudinary configuration or upload failure: " + ex.getMessage(), ex);
        }
    }
}
