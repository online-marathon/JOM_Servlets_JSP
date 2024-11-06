package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.Priority;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.repository.TaskRepository;

import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("Integration Tests for CreateTaskServlet")
public class CreateTaskServletTestIntegration {

    private static Tomcat tomcat;
    private static final String WEB_PORT = "8080";
    private static WebTestClient webTestClient;

    @BeforeAll
    public static void startServer() throws LifecycleException {
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

        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + WEB_PORT).build();
    }

    @AfterAll
    public static void stopServer() throws LifecycleException {
        tomcat.stop();
        tomcat.destroy();
    }

    @Test
    @DisplayName("GET /create-task should return task creation page")
    public void testGetRequestDisplaysPage() {
        webTestClient.get()
                .uri("/create-task")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(responseBody).contains("<h1>Create Task</h1>");
                });
    }

    @Test
    @DisplayName("POST /create-task should redirect when task is created")
    public void testValidPostRequest() {
        webTestClient.post()
                .uri("/create-task")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("title", "Task #3").with("priority", "MEDIUM"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/tasks-list");
    }

    @Test
    @DisplayName("POST /create-task should return error when duplicate task is created")
    public void testInvalidPostRequest() {

        TaskRepository.getTaskRepository().create(new Task("Task #2", Priority.MEDIUM));

        webTestClient.post()
                .uri("/create-task")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("title", "Task #2").with("priority", "MEDIUM"))
                .exchange()
                .expectStatus().isOk()  // Expect to stay on the same page due to error
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(responseBody).contains("Task with a given name already exists!");
                    assertThat(responseBody).contains("value=\"Task #2\"");
                    assertThat(responseBody).contains("value=\"MEDIUM\" selected");
                });
    }

    @Test
    @DisplayName("Repository should successfully create a unique task")
    public void testRepositoryCreateTask() {
        TaskRepository taskRepository = TaskRepository.getTaskRepository();
        Task task = new Task("Unique Task", Priority.MEDIUM);

        boolean created = taskRepository.create(task);

        assertThat(created).isTrue();
        assertThat(taskRepository.checkByName("Unique Task")).isNotNull();
    }

}
