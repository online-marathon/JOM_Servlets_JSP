package com.softserve.itacademy.controller;

import com.softserve.itacademy.repository.TaskRepository;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DeleteTaskServletTest {

    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private RequestDispatcher requestDispatcher;

    @InjectMocks
    private final DeleteTaskServlet deleteTaskServlet = new DeleteTaskServlet();

    @Before
    public void initialize() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("DeleteTaskServlet should delete task if ID is valid")
    public void testCorrectTaskDelete() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn("3");
        when(taskRepository.delete(anyInt())).thenReturn(true);

        deleteTaskServlet.doGet(request, response);

        verify(taskRepository, times(1)).delete(3);
        verify(response, times(1)).sendRedirect(anyString());
    }

    @Test
    @DisplayName("DeleteTaskServlet should show error message if task ID is invalid")
    public void testInvalidTaskDelete() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn("5");
        when(taskRepository.delete(anyInt())).thenReturn(false);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);

        deleteTaskServlet.doGet(request, response);

        verify(request, times(1)).setAttribute(eq("errorMessage"), eq("Task with ID '5' not found in To-Do List!"));
        verify(requestDispatcher, times(1)).forward(request, response);
    }
}
