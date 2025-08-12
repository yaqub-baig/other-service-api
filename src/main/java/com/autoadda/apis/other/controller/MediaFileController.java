package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.MediaFile;
import com.autoadda.apis.other.response.GenericResponse;
import com.autoadda.apis.other.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/mediafiles")
@RequiredArgsConstructor
public class MediaFileController {
    private final MediaFileService mediaFileService;
    @PostMapping("/upload")
    public List<MediaFile> upload(@RequestPart("files") List<MultipartFile> files) throws IOException {
        if ( files.stream ( ).anyMatch ( f -> f.getSize ( ) > 10 * 1024 * 1024 ) ) {
            throw new IllegalArgumentException ( "File size must be less than 10MB" );
        }
        return mediaFileService.upload ( files );
    }

    @DeleteMapping("/delete-image/{id}")
    public ResponseEntity<GenericResponse> deleteImage(@PathVariable("id") Long id) {
        return ResponseEntity.ok ( mediaFileService.delete ( id ) );
    }

}
