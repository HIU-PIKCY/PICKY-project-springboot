package com.picky.global.s3;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test/s3")
@Tag(name = "S3 테스트")
@RequiredArgsConstructor
public class S3TestController {

    private final S3Service s3Service;

    @Operation(summary = "파일 업로드 테스트")
    @PostMapping(value = "/upload/{filePath}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
        @PathVariable FilePath filePath,
        @RequestPart("file") MultipartFile file) {

        String fileUrl = s3Service.uploadFile(file, filePath);
        return ResponseEntity.ok(fileUrl);
    }

    @Operation(summary = "파일 삭제 테스트")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestBody S3DeleteRequestDto request) {
        s3Service.deleteFile(request.getFileUrl());
        return ResponseEntity.noContent().build();
    }

    @Getter
    @NoArgsConstructor
    static class S3DeleteRequestDto {
        private String fileUrl;
    }
}
