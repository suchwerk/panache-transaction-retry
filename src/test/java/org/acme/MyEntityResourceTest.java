package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class MyEntityResourceTest {

    @Test
    public void testHelloEndpoint() {
        given().when().get("/create").then().statusCode(200).body(is("{}"));
        List<MyEntity> list = new ArrayList<>();
        given().when().get("/list").then().statusCode(200).extract().as(list.getClass());
        assertTrue(list.size() == 1);
    }

}