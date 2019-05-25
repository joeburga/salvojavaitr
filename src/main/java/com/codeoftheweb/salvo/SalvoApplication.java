package com.codeoftheweb.salvo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class);

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository,
									  GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository,
									  SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
		return (args) -> {

			Player player1 = new Player("Jack Bauer","j.bauer@ctu.gov",passwordEncoder().encode("24"));
			Player player2 = new Player("Chloe O'Brian","c.obrian@ctu.gov",passwordEncoder().encode("42"));
			Player player3 = new Player("Kim Bauer","kim_bauer@gmail.com",passwordEncoder().encode("kb"));
			Player player4 = new Player("Tony Almeida","t.almeida@ctu.gov",passwordEncoder().encode("mole"));


			Date date1 = new Date();
			Date date2 = new Date();
			Date date3 = new Date();
			Date date4 = new Date();

			//date = Date.from(date.toInstant().plusSeconds(3600));
			Game game1 = new Game(Date.from(date1.toInstant().plusSeconds(3600)));
			Game game2 = new Game(Date.from(date2.toInstant().plusSeconds(7200)));
			Game game3 = new Game(Date.from(date3.toInstant().plusSeconds(10800)));
			Game game4 = new Game(Date.from(date4.toInstant().plusSeconds(14400)));

			// save a couple of Players
			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);

			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);
			gameRepository.save(game4);

			GamePlayer gamePlayer1 = new GamePlayer(game1,player1);
			GamePlayer gamePlayer2 = new GamePlayer(game1,player2);
			GamePlayer gamePlayer3 = new GamePlayer(game2,player4);
			GamePlayer gamePlayer4 = new GamePlayer(game2,player1);
			GamePlayer gamePlayer5 = new GamePlayer(game3,player3);
			GamePlayer gamePlayer6 = new GamePlayer(game3,player2);
			GamePlayer gamePlayer7 = new GamePlayer(game4,player4);
			GamePlayer gamePlayer8 = new GamePlayer(game4,player1);


			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);
			gamePlayerRepository.save(gamePlayer6);
			gamePlayerRepository.save(gamePlayer7);
			gamePlayerRepository.save(gamePlayer8);


			List<String> shipLocation1 = new ArrayList<>();
			shipLocation1.add("H2");
			shipLocation1.add("H3");
			shipLocation1.add("H4");

			List<String> shipLocation2 = new ArrayList<>();
			shipLocation2.add("F3");
			shipLocation2.add("F4");
			shipLocation2.add("F5");

			List<String> shipLocation3 = new ArrayList<>();
			shipLocation3.add("B4");
			shipLocation3.add("B5");

			List<String> shipLocation4 = new ArrayList<>();
			shipLocation4.add("A1");
			shipLocation4.add("A2");
			shipLocation4.add("A3");
			shipLocation4.add("A4");
			shipLocation4.add("A5");

			List<String> shipLocation5 = new ArrayList<>();
			shipLocation5.add("B1");
			shipLocation5.add("C1");
			shipLocation5.add("D1");
			shipLocation5.add("E1");

			Ship ship1 = new Ship("Destroyer",gamePlayer1,shipLocation1);
			Ship ship2 = new Ship("Submarine",gamePlayer2,shipLocation2);
			Ship ship3 = new Ship("PatrolBoat",gamePlayer3,shipLocation3);
			Ship ship4 = new Ship("Carrier",gamePlayer4,shipLocation4);
			Ship ship5 = new Ship("Battleship",gamePlayer5,shipLocation5);
			Ship ship6 = new Ship("Battleship",gamePlayer6,shipLocation1);
			Ship ship7 = new Ship("PatrolBoat",gamePlayer2,shipLocation3);
			Ship ship8 = new Ship("Carrier",gamePlayer3,shipLocation2);
			Ship ship9 = new Ship("Submarine",gamePlayer4,shipLocation1);
			Ship ship10 = new Ship("Destroyer",gamePlayer5,shipLocation2);
			Ship ship11 = new Ship("PatrolBoat",gamePlayer6,shipLocation5);
			Ship ship12 = new Ship("Battleship",gamePlayer7,shipLocation1);
			Ship ship13 = new Ship("Carrier",gamePlayer8,shipLocation2);
			Ship ship14 = new Ship("Battleship",gamePlayer1,shipLocation3);
			Ship ship15 = new Ship("Submarine",gamePlayer7,shipLocation4);
			Ship ship16 = new Ship("Destroyer",gamePlayer8,shipLocation5);

			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);
			shipRepository.save(ship5);
			shipRepository.save(ship6);
			shipRepository.save(ship7);
			shipRepository.save(ship8);
			shipRepository.save(ship9);
			shipRepository.save(ship10);
			shipRepository.save(ship11);
			shipRepository.save(ship12);
			shipRepository.save(ship13);
			shipRepository.save(ship14);
			shipRepository.save(ship15);
			shipRepository.save(ship16);


			List<String> salvoLocation1 = new ArrayList<>();
			salvoLocation1.add("B5");
			salvoLocation1.add("C5");
			salvoLocation1.add("F1");

			List<String> salvoLocation2 = new ArrayList<>();
			salvoLocation2.add("F5");
			salvoLocation2.add("F6");

			List<String> salvoLocation3 = new ArrayList<>();
			salvoLocation3.add("A2");
			salvoLocation3.add("A4");
			salvoLocation3.add("G6");

			List<String> salvoLocation4 = new ArrayList<>();
			salvoLocation4.add("A3");
			salvoLocation4.add("H6");

			List<String> salvoLocation5 = new ArrayList<>();
			salvoLocation5.add("G6");
			salvoLocation5.add("H6");
			salvoLocation5.add("A4");

			List<String> salvoLocation6 = new ArrayList<>();
			salvoLocation6.add("B4");
			salvoLocation6.add("B5");
			salvoLocation6.add("B6");

			List<String> salvoLocation7 = new ArrayList<>();
			salvoLocation7.add("E1");
			salvoLocation7.add("H3");
			salvoLocation7.add("A2");

			List<String> salvoLocation8 = new ArrayList<>();
			salvoLocation8.add("B5");
			salvoLocation8.add("D5");
			salvoLocation8.add("C7");

			List<String> salvoLocation9 = new ArrayList<>();
			salvoLocation9.add("C5");
			salvoLocation9.add("C6");

			List<String> salvoLocation10 = new ArrayList<>();
			salvoLocation10.add("H1");
			salvoLocation10.add("H2");
			salvoLocation10.add("H3");

			Salvo salvo1 = new Salvo(gamePlayer1,1,salvoLocation1);
			Salvo salvo2 = new Salvo(gamePlayer2,2,salvoLocation6);
			Salvo salvo3 = new Salvo(gamePlayer3,2,salvoLocation2);
			Salvo salvo4 = new Salvo(gamePlayer4,2,salvoLocation7);
			Salvo salvo5 = new Salvo(gamePlayer5,1,salvoLocation3);
			Salvo salvo6 = new Salvo(gamePlayer6,1,salvoLocation8);
			Salvo salvo7 = new Salvo(gamePlayer2,1,salvoLocation10);
			Salvo salvo8 = new Salvo(gamePlayer1,2,salvoLocation2);


			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);
			salvoRepository.save(salvo6);
			salvoRepository.save(salvo7);
			salvoRepository.save(salvo8);

			float win = 1;
			float loss = 0;
			float draw = (float)0.5;

			Score score1 = new Score(game1,player1,win,date4);
			Score score2 = new Score(game1,player2,win,date1);
			Score score3 = new Score(game2,player3,win,date2);
			Score score4 = new Score(game2,player2,draw,date2);
			Score score5 = new Score(game3,player3,loss,date2);
			Score score6 = new Score(game3,player1,win,date2);
			Score score7 = new Score(game4,player4,draw,date2);
			Score score8 = new Score(game4,player1,win,date2);

			scoreRepository.save(score1);
			scoreRepository.save(score2);
			scoreRepository.save(score3);
			scoreRepository.save(score4);
			scoreRepository.save(score5);
			scoreRepository.save(score6);
			scoreRepository.save(score7);
			scoreRepository.save(score8);

		};
	}

}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName-> {
			Player player = playerRepository.findByEmail(inputName);
			if (player != null) {
				return new User(player.getEmail(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});
	}
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers( "/web/games_3.html").permitAll()
				.antMatchers( "/web/**").permitAll()
				.antMatchers( "/api/games.").permitAll()
				.antMatchers( "/api/players").permitAll()
				.antMatchers( "/api/game_view/**").hasAuthority("USER")
				.antMatchers( "/rest/*").denyAll()
				.anyRequest().permitAll();

		http.formLogin()
				.usernameParameter("email")
				.passwordParameter("password")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		// VER BASE DE DATOS H2
		http.headers().frameOptions().sameOrigin();

		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}


	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}



