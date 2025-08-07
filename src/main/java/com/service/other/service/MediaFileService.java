package com.service.other.service;


import com.service.other.entity.MediaFile;
import com.service.other.response.GenericResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaFileService {

    List<MediaFile> upload(List<MultipartFile> files) throws IOException;

    GenericResponse delete(Long mediaFileId);

    GenericResponse getMediaFileById(Long mediaFileId);
}
