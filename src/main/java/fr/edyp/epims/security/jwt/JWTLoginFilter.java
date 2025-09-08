/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */

package fr.edyp.epims.security.jwt;


import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;


public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final TokenAuthenticationService tokenSecretService;

    public JWTLoginFilter(String url, AuthenticationManager authManager,TokenAuthenticationService tokenSecretService) {
        super(request -> request.getRequestURI().equals(url));
        setAuthenticationManager(authManager);
        this.tokenSecretService = tokenSecretService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        //System.out.printf("JWTLoginFilter.attemptAuthentication: username/password= %s,%s", username, password);
        //System.out.println();

        return getAuthenticationManager()
                .authenticate(new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList()));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        //System.out.println("JWTLoginFilter.successfulAuthentication:");

        // Write Authorization to Headers of Response.
        tokenSecretService.addAuthentication(response, authResult.getName());

        //String authorizationString = response.getHeader("Authorization");

        //System.out.println("Authorization String=" + authorizationString);
    }

}