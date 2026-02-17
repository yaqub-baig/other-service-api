package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.MediaFileResponse;
import com.autoadda.apis.other.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> upload(@RequestPart("files") List<MultipartFile> files) throws IOException {
        if ( files.stream ( ).anyMatch ( f -> f.getSize ( ) > 10 * 1024 * 1024 ) ) {
            throw new IllegalArgumentException ( "File size must be less than 10MB" );
        }
        List<MediaFileResponse> responses = mediaFileService.upload ( files );
        return ResponseEntity.ok(ApiResponse.success("Files uploaded successfully", responses));
    }

    @DeleteMapping("/delete-image/{id}")
    public ResponseEntity<ApiResponse<?>> deleteImage(@PathVariable("id") Long id) {
        return ResponseEntity.ok ( mediaFileService.delete ( id ) );
    }

}
