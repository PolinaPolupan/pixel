package com.example.mypixel;

import com.example.mypixel.service.FilteringService;
import com.example.mypixel.service.GraphService;
import com.example.mypixel.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
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

    @MockitoBean
    @Qualifier("storageService")
    private StorageService storageService;

    @MockitoBean
    @Qualifier("tempStorageService")
    private StorageService tempStorageService;

    @MockitoBean
    private FilteringService filteringService;

    @Autowired
    private GraphService graphService;

    @Test
    public void testFullGraphProcessing() throws Exception {
//        String jsonContent = "{\"nodes\":[{\"type\":\"InputNode\",\"params\":{\"filename\":\"input.jpg\"}},{\"type\":\"OutputNode\",\"params\":{\"filename\":\"output.jpg\"}}]}";
//
//        Resource mockResource = mock(Resource.class);
//        when(storageService.loadAsResource(anyString())).thenReturn(mockResource);
//        when(tempStorageService.loadAsResource(anyString())).thenReturn(mockResource);
//
//        mockMvc.perform(post("/v1/graph")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonContent))
//                .andExpect(status().isOk());
//
//       // verify(storageService).loadAsResource("input.jpg");
//        verify(storageService).store(any(Resource.class), eq("output.jpg"));
    }
}
