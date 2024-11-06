package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.Priority;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.repository.TaskRepository;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ReadTaskServletTestUnit {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @InjectMocks
    private final ReadTaskServlet readTaskServlet = new ReadTaskServlet();

    @BeforeEach
    public void initialize() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("GET /read-task with valid ID should fetch task and forward to JSP")
    public void testCorrectTaskRead() throws ServletException, IOException {
        Task task = new Task("Sample Task", Priority.MEDIUM);

        when(request.getParameter("id")).thenReturn("3");
        when(taskRepository.read(3)).thenReturn(task);
        when(request.getRequestDispatcher("/WEB-INF/pages/read-task.jsp")).thenReturn(requestDispatcher);

        readTaskServlet.doGet(request, response);

        verify(taskRepository, times(1)).read(3);

        verify(request, times(1)).setAttribute("id", 3);
        verify(request, times(1)).setAttribute("title", "Sample Task");
        verify(request, times(1)).setAttribute("priority", Priority.MEDIUM);

        verify(requestDispatcher, times(1)).forward(request, response);
    }


    @Test
    @DisplayName("GET /read-task with invalid ID should set error message and forward to error JSP")
    public void testInvalidTaskRead() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn("5");
        when(taskRepository.read(anyInt())).thenReturn(null);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);

        readTaskServlet.doGet(request, response);

        verify(request, times(1)).setAttribute("errorMessage", "Task with ID '5' not found in To-Do List!");
        verify(request, times(1)).getRequestDispatcher("/WEB-INF/pages/error.jsp");
        verify(requestDispatcher, times(1)).forward(request, response);
    }
}
