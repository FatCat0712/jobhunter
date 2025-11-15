package vn.hoidanit.jobhunter.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hoidanit.jobhunter.domain.response.file.ResponseUploadFileDTO;
import vn.hoidanit.jobhunter.service.FileService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.StorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FileController {
    @Value("${hoidanit.upload-file.base-uri}")
    private String baseURI;

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @ApiMessage("Upload single file")
    @PostMapping("/files")
    public ResponseEntity<ResponseUploadFileDTO> upload(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder
    ) throws URISyntaxException, IOException, StorageException {
//        skip validate
            if(file == null || file.isEmpty()) {
                throw new StorageException("File is empty. Please upload a file");
            }

            String fileName = file.getOriginalFilename();
            List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "docx");

            boolean isValid = allowedExtensions.stream().anyMatch(item -> fileName.toLowerCase().endsWith(item));

            if(!isValid) {
                throw new StorageException("Invalid file extension. Only allows " + allowedExtensions);
            }

//        create a directory if not exists
        fileService.createDirectory(baseURI + folder);

//        store file
        String uploadFileName = fileService.store(file, folder);

        ResponseUploadFileDTO dto = new ResponseUploadFileDTO();
        dto.setFileName(uploadFileName);
        dto.setUploadedAt(Instant.now());

        return ResponseEntity.ok().body(dto);
    }

    @ApiMessage("Download a file")
    @GetMapping("/files")
    public ResponseEntity<Resource> download(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder
    ) throws StorageException, URISyntaxException, FileNotFoundException {
        if(fileName == null || folder == null) {
            throw new StorageException("Missing required params: (fileName or folder) in query params");
        }

//        check file exist (and not a directory)
        long fileLength = fileService.getFileLength(fileName, folder);
        if(fileLength == 0) {
            throw new StorageException("File with name = " + fileName + " not found.");
        }

//        download a file
       InputStreamResource resource =  fileService.getResource(fileName, folder);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);

    }

}
