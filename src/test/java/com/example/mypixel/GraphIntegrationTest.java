package com.example.mypixel;

import com.example.mypixel.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GraphIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    @Test
    public void testFullGraphProcessing() throws Exception {
        String jsonContent = "{\"nodes\":[{\"type\":\"InputNode\",\"params\":{\"filename\":\"input.jpg\"}},{\"type\":\"OutputNode\",\"params\":{\"filename\":\"output.jpg\"}}]}";

        Resource mockResource = mock(Resource.class);
        when(storageService.loadAsResource("input.jpg")).thenReturn(mockResource);

        mockMvc.perform(post("/v1/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk());

        verify(storageService).loadAsResource("input.jpg");
        verify(storageService).store(any(Resource.class), eq("output.jpg"));
    }
}
