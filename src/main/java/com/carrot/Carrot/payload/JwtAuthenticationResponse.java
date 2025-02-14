// src/main/java/com/example/demo/payload/JwtAuthenticationResponse.java
package com.carrot.Carrot.payload;

public class JwtAuthenticationResponse {
    private String token;
    public JwtAuthenticationResponse(String token) { this.token = token; }
    public String getToken() { return token; }
}
