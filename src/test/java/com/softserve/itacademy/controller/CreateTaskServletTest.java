package com.softserve.itacademy.controller;

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
import static org.mockito.Mockito.*;

@DisplayName("Tests for CreateTaskServlet")
public class CreateTaskServletTest {

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
        System.out.println("Configuring app with basedir: " + new File("./" + webappDirLocation).getAbsolutePath());

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
    public void initialize() {
        MockitoAnnotations.openMocks(this);
        TaskRepository.getTaskRepository().deleteAll();
    }

    @Test
    @DisplayName("GET /create-task should return task creation page with status 200")
    public void testValidGetRequest() {
        byte[] body = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + WEB_PORT)
                .build()
                .method(HttpMethod.GET)
                .uri("/create-task")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody()
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(body, "Response body should not be null");
        Assertions.assertTrue(body.length > 0, "Response body should not be empty");
    }

    @Test
    @DisplayName("POST /create-task with valid data should redirect with status 3xx")
    public void testValidPostRequest() throws ServletException, IOException {
        WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + WEB_PORT)
                .build()
                .method(HttpMethod.POST)
                .uri("/create-task")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("title", "Task #3").with("priority", "MEDIUM"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("POST /create-task with existing task title should return error message")
    public void testInvalidPostRequest() {
        WebTestClient.RequestHeadersSpec<?> requestHeaders = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + WEB_PORT)
                .build()
                .method(HttpMethod.POST)
                .uri("/create-task")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("title", "Task #2").with("priority", "MEDIUM"));

        byte[] body = requestHeaders.exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody()
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(body, "Response body should not be null");
        Assertions.assertTrue(body.length > 0, "Response body should not be empty");

        String strBody = new String(body);
        Assertions.assertTrue(strBody.contains("Task with a given name already exists!"), 
                              "Expected error message 'Task with a given name already exists!'");
        Assertions.assertTrue(strBody.contains("value=\"Task #2\""), 
                              "Expected the title input field to retain the entered value");
        Assertions.assertTrue(strBody.contains("value=\"MEDIUM\" selected"), 
                              "Expected the priority dropdown to retain the selected priority");
    }

    @Test
    @DisplayName("CreateTaskServlet should call repository create method with valid task data")
    public void testCorrectTaskCreate() throws ServletException, IOException {
        when(request.getParameter("title")).thenReturn("Task #3");
        when(request.getParameter("priority")).thenReturn("MEDIUM");
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        when(taskRepository.create(any(Task.class))).thenReturn(true);

        createTaskServlet.doPost(request, response);

        verify(taskRepository, times(1)).create(any(Task.class));
    }
}
