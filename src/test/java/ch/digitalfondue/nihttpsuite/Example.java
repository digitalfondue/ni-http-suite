/**
 * Copyright © 2020 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.nihttpsuite;


import javax.json.Json;
import java.io.IOException;

public class Example {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server.configure()
                .listenerPort(8080)
                .route()
                .get("/", (req, res) -> {
                    res.ok().html("Hello world <a href='/test'>test</a>" + (Json.createObjectBuilder().add("key", "value").build().toString()));
                })
                .get("/json", (req, res) -> {
                    res.ok().json(Json.createObjectBuilder().add("key", "value").build());
                })
                .get("/json-arr", (req, res) -> {
                    res.ok().json(Json.createArrayBuilder().add("key").add("value").build());
                })
                .get("/test", (req, res) -> {
                    res.ok().html("Test <a href='/'>home</a>");
                })
                .get("/test/{abc}", (req, res) -> {
                    res.ok().html(req.getPathVariable("abc"));
                })
                .get("/test/{abc}/{def}", (req, res) -> {
                    res.ok().body(req.getPathVariable("abc")+"|"+req.getPathVariable("def"), "text/plain");
                })
                .build()
                .start();
    }
}

