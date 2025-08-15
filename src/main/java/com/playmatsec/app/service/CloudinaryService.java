package com.playmatsec.app.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public Map<String, String> uploadImage(MultipartFile file, String folder) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", folder);
            options.put("resource_type", "auto");
            options.put("unique_filename", true);
            
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            
            String publicId = result.get("public_id").toString();
            String format = result.get("format").toString();
            String secureUrl = result.get("secure_url").toString();
            
            String thumbnailUrl = cloudinary.url()
                .transformation(new Transformation()
                    .width(200)
                    .height(200)
                    .crop("fill")
                    .gravity("face"))
                .format(format)
                .generate(publicId);
            
            String watermarkUrl = cloudinary.url()
                .transformation(new Transformation()
                    .overlay("watermark")
                    .gravity("south_east")
                    .width(0.5)
                    .opacity(50))
                .format(format)
                .generate(publicId);
            
            return Map.of(
                "url", secureUrl,
                "thumbnail", thumbnailUrl,
                "watermark", watermarkUrl,
                "publicId", publicId
            );
        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary", e);
            return null;
        }
    }

    public boolean deleteImage(String publicId) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("invalidate", true);
            cloudinary.uploader().destroy(publicId, options);
            return true;
        } catch (IOException e) {
            log.error("Error deleting image from Cloudinary", e);
            return false;
        }
    }
}
