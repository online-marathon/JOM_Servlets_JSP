package com.softserve.itacademy.controller;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        CreateTaskServletTest.class,
        ReadTaskServletTest.class,
        UpdateTaskServletTest.class,
        DeleteTaskServletTest.class,
        TasksListServletTest.class
})
public class ToDoListWebAppSuite {
 
}
