package com.bub6le.systemmonitoring.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @InjectMocks
    private MainController mainController;

    @Test
    @DisplayName("测试首页")
    void testIndex() {
        // When
        String result = mainController.index();

        // Then
        assertEquals("index", result);
    }

    @Test
    @DisplayName("测试仪表板页面")
    void testDashboard() {
        // When
        String result = mainController.dashboard();

        // Then
        assertEquals("dashboard", result);
    }
}