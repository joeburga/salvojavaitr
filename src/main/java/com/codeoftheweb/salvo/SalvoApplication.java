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
	public CommandLineRunner initData(PlayerRepository playerRepository,
                                      GameRepository gameRepository,
									  GamePlayerRepository gamePlayerRepository,
                                      ShipRepository shipRepository,
									  SalvoRepository salvoRepository,
                                      ScoreRepository scoreRepository) {
		return (args) -> {

			Player player1 = new Player("Jack Bauer","j.bauer@ctu.gov",passwordEncoder().encode("24"));
			Player player2 = new Player("Chloe O'Brian","c.obrian@ctu.gov",passwordEncoder().encode("42"));
			Player player3 = new Player("Kim Bauer","kim_bauer@gmail.com",passwordEncoder().encode("kb"));
			Player player4 = new Player("Tony Almeida","t.almeida@ctu.gov",passwordEncoder().encode("mole"));


			Date date = new Date();
			int hora = 3600;

			//date = Date.from(date.toInstant().plusSeconds(3600));
			Game game1 = new Game(Date.from(date.toInstant().plusSeconds(0)));
			Game game2 = new Game(Date.from(date.toInstant().plusSeconds(hora)));
			Game game3 = new Game(Date.from(date.toInstant().plusSeconds(hora * 2)));
			Game game4 = new Game(Date.from(date.toInstant().plusSeconds(hora * 3)));
			Game game5 = new Game(Date.from(date.toInstant().plusSeconds(hora * 4)));
			Game game6 = new Game(Date.from(date.toInstant().plusSeconds(hora * 5)));

			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);

			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);
			gameRepository.save(game4);
			gameRepository.save(game5);
			gameRepository.save(game6);

			GamePlayer gamePlayer1 = new GamePlayer(game1,player1);
			GamePlayer gamePlayer2 = new GamePlayer(game1,player2);
			GamePlayer gamePlayer3 = new GamePlayer(game2,player1);
			GamePlayer gamePlayer4 = new GamePlayer(game2,player2);
			GamePlayer gamePlayer5 = new GamePlayer(game3,player2);
			GamePlayer gamePlayer6 = new GamePlayer(game3,player4);
			GamePlayer gamePlayer7 = new GamePlayer(game4,player2);
			GamePlayer gamePlayer8 = new GamePlayer(game4,player1);
			GamePlayer gamePlayer9 = new GamePlayer(game5,player4);
			GamePlayer gamePlayer10 = new GamePlayer(game5,player1);
			GamePlayer gamePlayer11 = new GamePlayer(game6,player3);
			//GamePlayer gamePlayer12 = new GamePlayer(game6,SIN OPONENTE);

			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);
			gamePlayerRepository.save(gamePlayer6);
			gamePlayerRepository.save(gamePlayer7);
			gamePlayerRepository.save(gamePlayer8);
			gamePlayerRepository.save(gamePlayer9);
			gamePlayerRepository.save(gamePlayer10);
			gamePlayerRepository.save(gamePlayer11);


			List<String> shipLocation1 = new ArrayList<>();
			shipLocation1.add("H2");
			shipLocation1.add("H3");
			shipLocation1.add("H4");
			List<String> shipLocation3 = new ArrayList<>();
			shipLocation3.add("E1");
			shipLocation3.add("F1");
			shipLocation3.add("G1");
			List<String> shipLocation7 = new ArrayList<>();
			shipLocation7.add("B5");
			shipLocation7.add("C5");
			shipLocation7.add("D5");
			List<String> shipLocation9 = new ArrayList<>();
			shipLocation9.add("A2");
			shipLocation9.add("A3");
			shipLocation9.add("A4");

			List<String> shipLocation4 = new ArrayList<>();
			shipLocation4.add("B1");
			shipLocation4.add("C1");
			shipLocation4.add("D1");
			shipLocation4.add("E1");

			List<String> shipLocation5 = new ArrayList<>();
			shipLocation5.add("A1");
			shipLocation5.add("A2");
			shipLocation5.add("A3");
			shipLocation5.add("A4");
			shipLocation5.add("A5");

			List<String> shipLocation2 = new ArrayList<>();
			shipLocation2.add("B4");
			shipLocation2.add("B5");
			List<String> shipLocation6 = new ArrayList<>();
			shipLocation6.add("F1");
			shipLocation6.add("F2");
			List<String> shipLocation8 = new ArrayList<>();
			shipLocation8.add("C6");
			shipLocation8.add("C7");
			List<String> shipLocation10 = new ArrayList<>();
			shipLocation10.add("G6");
			shipLocation10.add("H6");

			// Ordenado por GAMEs.
			Ship ship1 = new Ship("Destroyer",gamePlayer1,shipLocation1);
			Ship ship2 = new Ship("Submarine",gamePlayer1,shipLocation3);
			Ship ship3 = new Ship("PatrolBoat",gamePlayer1,shipLocation2);
			Ship ship4 = new Ship("Destroyer",gamePlayer2,shipLocation7);
			Ship ship5 = new Ship("PatrolBoat",gamePlayer2,shipLocation6);

			Ship ship6 = new Ship("Destroyer",gamePlayer3,shipLocation7);
			Ship ship7 = new Ship("PatrolBoat",gamePlayer3,shipLocation8);
			Ship ship8 = new Ship("Submarine",gamePlayer4,shipLocation9);
			Ship ship9 = new Ship("PatrolBoat",gamePlayer4,shipLocation10);

			Ship ship10 = new Ship("Destroyer",gamePlayer5,shipLocation7);
			Ship ship11 = new Ship("PatrolBoat",gamePlayer5,shipLocation8);
			Ship ship12 = new Ship("Submarine",gamePlayer6,shipLocation9);
			Ship ship13 = new Ship("PatrolBoat",gamePlayer6,shipLocation10);

			Ship ship14 = new Ship("Destroyer",gamePlayer7,shipLocation7);
			Ship ship15 = new Ship("PatrolBoat",gamePlayer7,shipLocation8);
			Ship ship16 = new Ship("Submarine",gamePlayer8,shipLocation9);
			Ship ship17 = new Ship("PatrolBoat",gamePlayer8,shipLocation10);

			// Diferente al salvo-testbed.
			Ship ship18 = new Ship("Carrier",gamePlayer9,shipLocation5);
			Ship ship19 = new Ship("Battleship",gamePlayer9,shipLocation4);
			Ship ship20 = new Ship("Submarine",gamePlayer10,shipLocation1);
			Ship ship21 = new Ship("Battleship",gamePlayer10,shipLocation4);

			Ship ship22 = new Ship("Carrier",gamePlayer11,shipLocation5);
			Ship ship23 = new Ship("Battleship",gamePlayer11,shipLocation4);

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
			shipRepository.save(ship17);
			shipRepository.save(ship18);
			shipRepository.save(ship19);
			shipRepository.save(ship20);
			shipRepository.save(ship21);
			shipRepository.save(ship22);
			shipRepository.save(ship23);


			List<String> salvoLocation1 = new ArrayList<>();
			salvoLocation1.add("B5");
			salvoLocation1.add("C5");
			salvoLocation1.add("F1");

			List<String> salvoLocation2 = new ArrayList<>();
			salvoLocation2.add("F2");
			salvoLocation2.add("D5");

			List<String> salvoLocation3 = new ArrayList<>();
			salvoLocation3.add("A2");
			salvoLocation3.add("A4");
			salvoLocation3.add("G6");

			List<String> salvoLocation4 = new ArrayList<>();
			salvoLocation4.add("H1");
			salvoLocation4.add("H2");
			salvoLocation4.add("H3");

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
			salvoLocation10.add("A3");
			salvoLocation10.add("H6");

			List<String> salvoLocation11 = new ArrayList<>();
			salvoLocation11.add("A2");
			salvoLocation11.add("A3");
			salvoLocation11.add("D8");

			List<String> salvoLocation12 = new ArrayList<>();
			salvoLocation12.add("E1");
			salvoLocation12.add("F2");
			salvoLocation12.add("G3");

			List<String> salvoLocation13 = new ArrayList<>();
			salvoLocation13.add("A3");
			salvoLocation13.add("A4");
			salvoLocation13.add("F7");

			List<String> salvoLocation14 = new ArrayList<>();
			salvoLocation14.add("B5");
			salvoLocation14.add("C6");
			salvoLocation14.add("H1");

			List<String> salvoLocation15 = new ArrayList<>();
			salvoLocation15.add("A2");
			salvoLocation15.add("G6");
			salvoLocation15.add("H6");

			List<String> salvoLocation16 = new ArrayList<>();
			salvoLocation16.add("C5");
			salvoLocation16.add("C7");
			salvoLocation16.add("D5");

			List<String> salvoLocation17 = new ArrayList<>();
			salvoLocation17.add("A1");
			salvoLocation17.add("A2");
			salvoLocation17.add("A3");

			List<String> salvoLocation18 = new ArrayList<>();
			salvoLocation18.add("B5");
			salvoLocation18.add("B6");
			salvoLocation18.add("C7");

			List<String> salvoLocation19 = new ArrayList<>();
			salvoLocation19.add("G6");
			salvoLocation19.add("G5");
			salvoLocation19.add("G8");

			List<String> salvoLocation20 = new ArrayList<>();
			salvoLocation20.add("C6");
			salvoLocation20.add("D6");
			salvoLocation20.add("E6");

			List<String> salvoLocation21 = new ArrayList<>();
			salvoLocation21.add("H1");
			salvoLocation21.add("H8");

			// Ordenado por GAMEs y TURNs.
			Salvo salvo1 = new Salvo(gamePlayer1,1,salvoLocation1);
			Salvo salvo2 = new Salvo(gamePlayer2,1,salvoLocation6);
			Salvo salvo3 = new Salvo(gamePlayer1,2,salvoLocation2);
			Salvo salvo4 = new Salvo(gamePlayer2,2,salvoLocation7);

			Salvo salvo5 = new Salvo(gamePlayer3,1,salvoLocation3);
			Salvo salvo6 = new Salvo(gamePlayer4,1,salvoLocation8);
			Salvo salvo7 = new Salvo(gamePlayer3,2,salvoLocation10);
			Salvo salvo8 = new Salvo(gamePlayer4,2,salvoLocation9);

			Salvo salvo9 = new Salvo(gamePlayer5,1,salvoLocation5);
			Salvo salvo10 = new Salvo(gamePlayer6,1,salvoLocation4);
			Salvo salvo11 = new Salvo(gamePlayer5,2,salvoLocation11);
			Salvo salvo12 = new Salvo(gamePlayer6,2,salvoLocation12);

			Salvo salvo13 = new Salvo(gamePlayer7,1,salvoLocation13);
			Salvo salvo14 = new Salvo(gamePlayer8,1,salvoLocation14);
			Salvo salvo15 = new Salvo(gamePlayer7,2,salvoLocation15);
			Salvo salvo16 = new Salvo(gamePlayer8,2,salvoLocation16);

			Salvo salvo17 = new Salvo(gamePlayer9,1,salvoLocation17);
			Salvo salvo18 = new Salvo(gamePlayer10,1,salvoLocation18);
			Salvo salvo19 = new Salvo(gamePlayer9,2,salvoLocation19);
			Salvo salvo20 = new Salvo(gamePlayer10,2,salvoLocation20);
			Salvo salvo21 = new Salvo(gamePlayer10,3,salvoLocation21);

			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);
			salvoRepository.save(salvo6);
			salvoRepository.save(salvo7);
			salvoRepository.save(salvo8);
			salvoRepository.save(salvo9);
			salvoRepository.save(salvo10);
			salvoRepository.save(salvo11);
			salvoRepository.save(salvo12);
			salvoRepository.save(salvo13);
			salvoRepository.save(salvo14);
			salvoRepository.save(salvo15);
			salvoRepository.save(salvo16);
			salvoRepository.save(salvo17);
			salvoRepository.save(salvo18);
			salvoRepository.save(salvo19);
			salvoRepository.save(salvo20);
			salvoRepository.save(salvo21);

			float win = 1;
			float loss = 0;
			float draw = (float)0.5;

			// finishDate media hora después de haber empezado el Game.
			Date finishDateGame1 = Date.from(game1.getDate().toInstant().plusSeconds(1800));
			Date finishDateGame2 = Date.from(game2.getDate().toInstant().plusSeconds(1800));
			Date finishDateGame3 = Date.from(game3.getDate().toInstant().plusSeconds(1800));
			Date finishDateGame4 = Date.from(game4.getDate().toInstant().plusSeconds(1800));
			Date finishDateGame5 = Date.from(game5.getDate().toInstant().plusSeconds(1800));
			Date finishDateGame6 = Date.from(game6.getDate().toInstant().plusSeconds(1800));

			Score score1 = new Score(game1,player1,win,finishDateGame1);
			Score score2 = new Score(game1,player2,loss,finishDateGame1);
			Score score3 = new Score(game2,player1,draw,finishDateGame2);
			Score score4 = new Score(game2,player2,draw,finishDateGame2);
			Score score5 = new Score(game3,player2,win,finishDateGame3);
			Score score6 = new Score(game3,player4,loss,finishDateGame3);
			Score score7 = new Score(game4,player2,draw,finishDateGame4);
			Score score8 = new Score(game4,player1,draw,finishDateGame4);

			//Score score9 = new Score(game5,player4,SIN DEFINIR,finishDateGame5);
			//Score score10 = new Score(game5,player1,SIN DEFINIR,finishDateGame5);
			//Score score11 = new Score(game6,player3,SIN DEFINIR,finishDateGame6);
			//Score score12 = new Score(game6,SIN OPONENTE,SIN DEFINIR,finishDateGame6);

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

/**
 * Configuración de las autorizaciones y roles de los usuarios.
 */
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

/**
 * Configuración de las URLs con permisos,
 * Configuración login y logout.
 */
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
        // Ir application.properties para ver las properties agregadas.
        http.headers().frameOptions().sameOrigin();

        // turn off checking for CSRF tokens
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



