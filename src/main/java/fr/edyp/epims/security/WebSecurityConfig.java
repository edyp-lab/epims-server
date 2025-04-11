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

package fr.edyp.epims.security;

import fr.edyp.epims.security.jwt.AuthEntryPointJwt;
import fr.edyp.epims.security.jwt.AuthTokenFilter;
import fr.edyp.epims.security.jwt.JWTAuthenticationFilter;
import fr.edyp.epims.security.jwt.JWTLoginFilter;
import fr.edyp.epims.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
@EnableAsync
@EnableWebSecurity
@EnableGlobalMethodSecurity(
		// securedEnabled = true,
		// jsr250Enabled = true,
		prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	UserDetailsServiceImpl userDetailsService;

	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;

	@Bean
	public AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}

	@Override
	public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		//BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		//return bCryptPasswordEncoder;
		return new PasswordEncoder() {
			public String encode(CharSequence var1) {
				String encode = var1.toString();
				return encode;


			}

			public boolean matches(CharSequence var1, String var2) {
				String encode = var1.toString();
				return var1.equals(var2);
			}

			public boolean upgradeEncoding(String encodedPassword) {
				return false;
			}
		};

		//return new BCryptPasswordEncoder();
	}

	@Override
	/*
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable()
			.exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			.authorizeRequests()
				.antMatchers("/api/auth/**").permitAll()
				.antMatchers("/api/test/**").permitAll()
				.antMatchers("/", "/**","/css/**", "/jss/**").permitAll()
			.anyRequest().authenticated();

		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
	}*/

	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
				// No need authentication.
				.antMatchers("/").permitAll() //
				.antMatchers(HttpMethod.POST, "/login").permitAll() //
				//.antMatchers(HttpMethod.GET, "/login").permitAll() // For Test on Browser
				.antMatchers(HttpMethod.GET, "/api/contacts").permitAll() // not secured URL
				.antMatchers(HttpMethod.GET, "/api/databaseversion").permitAll()
				.antMatchers(HttpMethod.GET, "/api/spectrometers").permitAll()
				.antMatchers(HttpMethod.GET, "/api/samplespecies").permitAll()
				.antMatchers(HttpMethod.GET, "/api/sampletypes").permitAll()
				.antMatchers(HttpMethod.GET, "/api/companies").permitAll()
				.antMatchers(HttpMethod.GET, "/api/actors").permitAll()
				.antMatchers(HttpMethod.GET, "/api/acquisitiontypes").permitAll()
				.antMatchers(HttpMethod.GET, "/api/fixfilelink").permitAll()
				.antMatchers(HttpMethod.GET, "/api/acquisitionsSearch/{searchText}/{acquisitionType}/{instrumentId}/{studyMemberActorKey}/{startDate}/{endDate}").permitAll()

				// Need authentication.
				.anyRequest().authenticated()
				//
				.and()
				//
				// Add Filter 1 - JWTLoginFilter
				//
				.addFilterBefore(new JWTLoginFilter("/login", authenticationManager()),
						UsernamePasswordAuthenticationFilter.class)
				//
				// Add Filter 2 - JWTAuthenticationFilter
				//
				.addFilterBefore(new JWTAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
	}
}
