package com.autoadda.apis.other.service;


import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.MediaFileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaFileService {

    List<MediaFileResponse> upload(List<MultipartFile> files) throws IOException;

    ApiResponse<String> delete(Long mediaFileId);

    ApiResponse<MediaFileResponse> getMediaFileById(Long mediaFileId);
}
