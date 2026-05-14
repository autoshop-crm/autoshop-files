package com.vladko.autoshopfilestorage.file;

import com.vladko.autoshopfilestorage.bucket.FileCategory;
import com.vladko.autoshopfilestorage.common.GlobalExceptionHandler;
import com.vladko.autoshopfilestorage.config.WebCorsConfiguration;
import com.vladko.autoshopfilestorage.file.dto.FileMetadataResponse;
import com.vladko.autoshopfilestorage.file.dto.OwnerFilesResponse;
import com.vladko.autoshopfilestorage.file.dto.PresignedUrlResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@Import({GlobalExceptionHandler.class, WebCorsConfiguration.class})
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Test
    void uploadsMultipartFile() throws Exception {
        UUID fileId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                "test".getBytes()
        );
        when(fileService.upload(eq(FileCategory.ORDER_DOCUMENT), eq(OwnerType.ORDER), eq("order-1"), eq("uploader-1"), any()))
                .thenReturn(response(fileId));

        mockMvc.perform(multipart("/api/files")
                        .file(file)
                        .param("category", "ORDER_DOCUMENT")
                        .param("ownerType", "ORDER")
                        .param("ownerId", "order-1")
                        .param("uploadedBy", "uploader-1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(fileId.toString()))
                .andExpect(jsonPath("$.category").value("ORDER_DOCUMENT"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void listsFilesByOwner() throws Exception {
        UUID fileId = UUID.randomUUID();
        when(fileService.listByOwner(OwnerType.ORDER, "order-1", null, false, 0, 20))
                .thenReturn(new OwnerFilesResponse(List.of(response(fileId)), 0, 20, 1));

        mockMvc.perform(get("/api/files")
                        .param("ownerType", "ORDER")
                        .param("ownerId", "order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(fileId.toString()));
    }

    @Test
    void returnsPresignedUrl() throws Exception {
        UUID fileId = UUID.randomUUID();
        when(fileService.presignedDownloadUrl(fileId, 900))
                .thenReturn(new PresignedUrlResponse(fileId, "http://localhost:9000/documents/key", Instant.parse("2026-04-20T10:15:30Z"), 900));

        mockMvc.perform(post("/api/files/{fileId}/presigned-download-url", fileId)
                        .contentType("application/json")
                        .content("{\"ttlSeconds\":900}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://localhost:9000/documents/key"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900));
    }

    @Test
    void allowsCorsPreflightForUpload() throws Exception {
        mockMvc.perform(options("/api/files")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsStringIgnoringCase("authorization"),
                        org.hamcrest.Matchers.containsStringIgnoringCase("content-type")
                )));
    }

    private FileMetadataResponse response(UUID fileId) {
        Instant now = Instant.parse("2026-04-20T10:00:00Z");
        return new FileMetadataResponse(
                fileId,
                FileCategory.ORDER_DOCUMENT,
                OwnerType.ORDER,
                "order-1",
                "uploader-1",
                "invoice.pdf",
                "application/pdf",
                4,
                "checksum",
                FileStatus.AVAILABLE,
                now,
                now,
                null
        );
    }
}
