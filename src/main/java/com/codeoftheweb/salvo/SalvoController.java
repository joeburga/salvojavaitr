package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

/*    @Autowired
    private GamePlayerRepository repoGamePlayer;

    @RequestMapping("/gamePlayer")
    public List<GamePlayer> getAll() {
        return repoGamePlayer.findAll();
    }

    // Tarea 4 : Lista con los IDs
    @Autowired
    private GameRepository repoGame;

    @RequestMapping("/gamesIDs")
    public List<Long> getGamesIDs() {
        List<Game> games;
        List<Long> indices = new ArrayList<>();
        
        games = repoGame.findAll();

        for (Game game:games) {
            indices.add(game.getId());
        }

        return indices;
    }*/

    //Tarea 5 : Lista con los IDs y Fechas de creacion
/*
    @Autowired
    private GameRepository repoIDsFechas;

    @RequestMapping("/allGames")
    public List<Object> getAllGames() {
        return repoIDsFechas
                .findAll()
                .stream()
                .map(game -> makeGameDTO(game))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGameDTO(Game game) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
            dto.put("id", game.getId());
            dto.put("created", game.getDate());

        return dto;
    }
*/

    //Tarea 6  : Lista con los IDs de GamePlayers , Fechas de creacion y GamePlayers(con sus IDs y emails de los players)
    @Autowired
    private GameRepository repoAllGame;

    @RequestMapping("/games")
    public List<Object> getAllGamePlayers() {
        return repoAllGame
                .findAll()
                .stream()
                .map(game -> makeGameDTO(game))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGameDTO(Game game) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("id", game.getId());
        dto.put("creationDate", game.getDate());
        dto.put("gamePlayers",makeGamePlayersListaDTO(game.getGamePlayers()));
        //dto.put("ships", getGameView(game.getId()));

        return dto;
    }

    //Itero sobre la lista anterior
    public List<Object> makeGamePlayersListaDTO(Set<GamePlayer> gamePlayers) {
        return gamePlayers
                .stream()
                .map(gamePlayer -> makeGamePlayersDTO(gamePlayer))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGamePlayersDTO(GamePlayer gamePlayer) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));

        return dto;
    }

    public Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getUserName());
        return dto;
    }

    // Para que me muestre id-player y emails de los players, cambiar nombre makeGamePlayerDTO por makePlayerDTO
    // y el GamePlayerRepository por PlayerRepository
/*        private Map<String, Object> makePlayerDTO(Player player) {
            Map<String, Object> dto = new LinkedHashMap<String, Object>();
            dto.put("id-player", player.getId());
            dto.put("name", player.getUserName());
            return dto;
        }*/

    //Task 3 - tarea 2
    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @RequestMapping("/game_view/{id}")
    public Map<String,Object> getGameView(@PathVariable Long id) {
        return gameViewDTO(gamePlayerRepository.findById(id).get());
    }

    private Map<String,Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getDate());
        //estoy en un gameplayer luiego getgame para ir al juego donde estoy,
        // luego desde el game traig los gamePlayers.
        dto.put("gamePlayers",makeGamePlayersListaDTO(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships",gamePlayer.getShips()); //makeShipsListaDTO(gamePlayer.getShips()
        //dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream().map(gamePlayer1 -> gamePlayer1.getSalvoes()).); //hace dto
        return dto;
    }

/*    private Map<String,Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("ships",makeShipsListaDTO(gamePlayer.getShips()));

        return dto;
    }
    //Itero sobre la lista anterior
    public List<Object> makeShipsListaDTO(Set<Ship> ships) {
        return ships
                .stream()
                .map(ship -> makeShipsDTO(ship))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeShipsDTO(Ship ship) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());

        return dto;
    }
 */
}
