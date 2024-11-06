package com.softserve.itacademy.controller;

import com.softserve.itacademy.repository.TaskRepository;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// URL path for accessing this servlet
public class UpdateTaskServlet extends HttpServlet {

    private TaskRepository taskRepository;

    @Override
    public void init() {
        taskRepository = TaskRepository.getTaskRepository();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // Logic to handle GET requests, possibly for fetching a task to update
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        // Logic to handle POST requests, typically for updating task data
    }
}
