package com.codeoftheweb.salvo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class);

	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository,
									  GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository,
									  SalvoRepository salvoRepository) {
		return (args) -> {

			Player player1 = new Player("player1@gmail.com");
			Player player2 = new Player("player2@gmail.com");
			Player player3 = new Player("player3@gmail.com");
			Player player4 = new Player("player4@gmail.com");

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
			GamePlayer gamePlayer3 = new GamePlayer(game3,player3);
			GamePlayer gamePlayer4 = new GamePlayer(game4,player4);

			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);


			List<String> shipLocation1 = new ArrayList<>();
			shipLocation1.add("B5");
			shipLocation1.add("B6");
			shipLocation1.add("B7");
			shipLocation1.add("B8");
			shipLocation1.add("B9");

			List<String> shipLocation2 = new ArrayList<>();
			shipLocation2.add("F3");
			shipLocation2.add("G3");
			shipLocation2.add("H3");

			List<String> shipLocation3 = new ArrayList<>();
			shipLocation3.add("I5");
			shipLocation3.add("I6");
			shipLocation3.add("I7");
			shipLocation3.add("I8");
			shipLocation3.add("I9");

			List<String> shipLocation4 = new ArrayList<>();
			shipLocation4.add("A3");
			shipLocation4.add("B3");
			shipLocation4.add("C3");

			Ship ship1 = new Ship("destroyer",gamePlayer1,shipLocation1);
			Ship ship2 = new Ship("patrol boat",gamePlayer2,shipLocation2);
			Ship ship3 = new Ship("cruiser",gamePlayer3,shipLocation3);
			Ship ship4 = new Ship("random",gamePlayer4,shipLocation4);

			Ship ship5 = new Ship("thanos",gamePlayer1,shipLocation2);
			Ship ship6 = new Ship("thor",gamePlayer2,shipLocation1);

			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);
			shipRepository.save(ship5);
			shipRepository.save(ship6);

			List<String> salvoLocation1 = new ArrayList<>();
			salvoLocation1.add("H1");
			salvoLocation1.add("A2");

			List<String> salvoLocation2 = new ArrayList<>();
			salvoLocation2.add("C5");
			salvoLocation2.add("E6");

			List<String> salvoLocation3 = new ArrayList<>();
			salvoLocation3.add("B4");
			salvoLocation3.add("D8");


			Salvo salvo1 = new Salvo(gamePlayer1,1,salvoLocation1);
			Salvo salvo2 = new Salvo(gamePlayer1,2,salvoLocation2);
			Salvo salvo3 = new Salvo(gamePlayer1,3,salvoLocation3);


			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);


		};
	}

}
