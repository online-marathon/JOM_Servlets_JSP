package com.softserve.itacademy.controller;

import com.softserve.itacademy.repository.TaskRepository;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


// Specifies the URL path for this servlet
public class DeleteTaskServlet extends HttpServlet {

    private TaskRepository taskRepository;

    @Override
    public void init() {
        taskRepository = TaskRepository.getTaskRepository();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // Handle GET requests - could be used for a confirmation page or message
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        // Handle POST requests - typically for deleting a task
    }
}
