package org.lightchurch.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.lightchurch.LightchurchApp;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = LightchurchApp.class)
@WebAppConfiguration
public class CucumberTestContextConfiguration {}
