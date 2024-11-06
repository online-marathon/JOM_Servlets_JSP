package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.Priority;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.repository.TaskRepository;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for CreateTaskServlet")
public class CreateTaskServletTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @InjectMocks
    private final CreateTaskServlet createTaskServlet = new CreateTaskServlet();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TaskRepository.getTaskRepository().deleteAll();
    }

    @Test
    @DisplayName("GET /create-task should display task creation page")
    void testGetRequestDisplaysPage() throws ServletException, IOException {
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);

        createTaskServlet.doGet(request, response);

        verify(request, times(1)).getRequestDispatcher("/WEB-INF/pages/create-task.jsp");
        verify(requestDispatcher, times(1)).forward(request, response);
    }

    @Test
    @DisplayName("POST /create-task should create a new task if valid data is provided")
    void testValidPostRequest() throws ServletException, IOException {
        when(request.getParameter("title")).thenReturn("Task #3");
        when(request.getParameter("priority")).thenReturn("MEDIUM");
        when(taskRepository.create(any(Task.class))).thenReturn(true);

        createTaskServlet.doPost(request, response);

        verify(taskRepository, times(1)).create(any(Task.class));
        verify(response, times(1)).sendRedirect("/tasks-list");
    }

    @Test
    @DisplayName("POST /create-task should fail when task with the same title exists")
    void testInvalidPostRequestWithExistingTitle() throws ServletException, IOException {
        when(request.getParameter("title")).thenReturn("Task #2");
        when(request.getParameter("priority")).thenReturn("MEDIUM");
        when(taskRepository.create(any(Task.class))).thenReturn(false);
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);

        createTaskServlet.doPost(request, response);

        verify(request, times(1)).setAttribute(eq("errorMessage"), eq("Task with a given name already exists!"));
        verify(request, times(1)).getRequestDispatcher("/WEB-INF/pages/create-task.jsp");
        verify(requestDispatcher, times(1)).forward(request, response);
    }

    @Test
    @DisplayName("Repository should successfully create a unique task")
    void testRepositoryCreateTask() {
        Task task = new Task("Unique Task", Priority.MEDIUM);
        when(taskRepository.create(any(Task.class))).thenReturn(true);

        boolean created = taskRepository.create(task);
        Assertions.assertTrue(created, "Expected task to be created successfully");

        verify(taskRepository, times(1)).create(task);
    }

}
