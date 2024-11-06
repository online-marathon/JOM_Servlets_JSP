package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.Priority;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.repository.TaskRepository;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UpdateTaskServletTest {

    private static Tomcat tomcat;
    private static final String WEB_PORT = "8080";

    @BeforeAll
    public static void startServer() throws ServletException, LifecycleException {
        String webappDirLocation = "src/main/webapp/";
        tomcat = new Tomcat();

        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = WEB_PORT;
        }

        tomcat.setPort(Integer.parseInt(webPort));

        StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
        ctx.getServletContext().setAttribute(Globals.ALT_DD_ATTR, webappDirLocation + "WEB-INF/web.xml");

        File additionWebInfClasses = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        tomcat.start();
    }

    @AfterAll
    public static void stopServer() throws LifecycleException {
        tomcat.stop();
        tomcat.destroy();
    }

    @Mock
    private Task task;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @InjectMocks
    private final UpdateTaskServlet updateTaskServlet = new UpdateTaskServlet();

    @BeforeEach
    void initialize() {
        MockitoAnnotations.openMocks(this);
        TaskRepository.getTaskRepository().deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("GET /edit-task should display the edit page for valid ID")
    public void testValidGetRequest() {
        byte[] body = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + WEB_PORT)
                .build()
                .method(HttpMethod.GET)
                .uri("/edit-task?id=1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody().returnResult().getResponseBody();

        assert body != null;
        Assertions.assertTrue(body.length > 0);

        String strBody = new String(body);
        Assertions.assertTrue(strBody.contains("value=\"Task #1\""), "Expected value in input field but it was empty!");
        Assertions.assertTrue(strBody.contains("value=\"MEDIUM\" selected"), "Expected value in drop-down list but it was empty!");
    }

    @Test
    @Order(2)
    @DisplayName("POST /edit-task should update task and redirect for valid input")
    public void testValidPostRequest() throws ServletException, IOException {
        WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + WEB_PORT)
                .build()
                .method(HttpMethod.POST)
                .uri("/edit-task")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("id", "1")
                        .with("title", "Task #4")
                        .with("priority", "HIGH"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectBody().isEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("GET /edit-task with invalid ID should return 404 and error message")
    public void testInvalidGetRequest() {
        byte[] body = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + WEB_PORT)
                .build()
                .method(HttpMethod.GET)
                .uri("/edit-task?id=3")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody().returnResult().getResponseBody();

        assert body != null;
        Assertions.assertTrue(body.length > 0);

        String strBody = new String(body);
        Assertions.assertTrue(strBody.contains("Task with ID '3' not found in To-Do List!"), "Expected error message not found!");
    }

    @Test
    @Order(4)
    @DisplayName("POST /edit-task should display error for duplicate title")
    public void testInvalidPostRequest() {
        WebTestClient.RequestHeadersSpec<?> requestHeaders = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + WEB_PORT)
                .build()
                .method(HttpMethod.POST)
                .uri("/edit-task")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("id", "1")
                        .with("title", "Task #2")
                        .with("priority", "MEDIUM"));

        byte[] body = requestHeaders.exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody().returnResult().getResponseBody();

        assert body != null;
        Assertions.assertTrue(body.length > 0);

        String strBody = new String(body);
        Assertions.assertTrue(strBody.contains("Task with a given name already exists!"), "Expected error message not found!");
        Assertions.assertTrue(strBody.contains("value=\"Task #2\""), "Expected value in input field but it was empty!");
        Assertions.assertTrue(strBody.contains("value=\"MEDIUM\" selected"), "Expected value in drop-down list but it was empty!");
    }

    @Test
    @Order(5)
    @DisplayName("POST /edit-task should call repository update method correctly")
    public void testCorrectTaskUpdate() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn("1");
        when(request.getParameter("title")).thenReturn("Task #3");
        when(request.getParameter("priority")).thenReturn("MEDIUM");
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(taskRepository.update(any(Task.class))).thenReturn(true);

        updateTaskServlet.doPost(request, response);

        verify(taskRepository, times(1)).update(any(Task.class));
    }
}
