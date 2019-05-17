package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    //Tarea 6  : Lista con los IDs de GamePlayers , Fechas de creacion y GamePlayers(con sus IDs y emails de los players)
    @Autowired
    private GameRepository gameRepository;

    @RequestMapping("/games")
    public Map<String, Object> makeLoggedPlayer(Authentication authentication) {

        Map<String, Object> dto = new LinkedHashMap<>();
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);

        if (authenticatedPlayer == null)
            dto.put("player", "Guest");
        else
            dto.put("player", makePlayerDTO(authenticatedPlayer));


        dto.put("games", getAllGamePlayers());

        return dto;
    }
    public List<Object> getAllGamePlayers() {
        return gameRepository
                .findAll()
                .stream()
                .map(game -> makeGameDTO(game))
                .collect(Collectors.toList());
    }
    private Player getAuthentication(Authentication authentication) {

        if(authentication == null || authentication instanceof AnonymousAuthenticationToken){
            return null;
        } else {

           return playerRepository.findByEmail(authentication.getName());
        }
    }


    private Map<String, Object> makeGameDTO(Game game) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("id", game.getId());
        dto.put("creationDate", game.getDate());
        dto.put("gamePlayers",makeGamePlayersListaDTO(game.getGamePlayers()));
        //Task 5
        dto.put("scores", makeListGamesScores(game.getScores()));

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
        dto.put("gpid", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));

        return dto;
    }

    public Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getEmail());
        //Task 5
        dto.put("score", makeScoresList(player));
        return dto;
    }

    //Task 5
    public List<Object> makeListGamesScores(Set<Score> scores) {
        return scores
                .stream()
                .map(score -> makeScoreDTO(score))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeScoreDTO(Score score) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("playerID", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("finishDate", score.getFinishDate());

        return dto;
    }

    @Autowired
    private PlayerRepository playerRepository;

    @RequestMapping("/leaderBoard")
    public List<Object> makeLeaderBoard() {
        return playerRepository
                .findAll()
                .stream()
                .map(player -> makePlayerDTO(player))
                .collect(Collectors.toList());
    }

    public Map<String, Object> makeScoresList(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("name", player.getUserName());
        dto.put("total", player.getScore(player));
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost", player.getLosses(player.getScores()));
        dto.put("tied", player.getDraws(player.getScores()));

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
        dto.put("ships", makeShipsListaDTO(gamePlayer.getShips())); // gamePlayer.getShips()
        dto.put("salvoes", makeListGames(gamePlayer.getGame()));
        //gamePlayer.getGame().getGamePlayers().stream().map(gamePlayer1 -> makeSalvoesListaDTO(gamePlayer1.getSalvoes()))
        dto.put("scores", makeListGamesScores(gamePlayer.getGame().getScores()));

        return dto;
    }
    public List<Object> makeShipsListaDTO(Set<Ship> ships) {
        return ships
                .stream()
                .map(ship -> makeShipDTO(ship))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeShipDTO(Ship ship) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());

        return dto;
    }

    private List<Map<String, Object>> makeListGames(Game game) {

        List<Map<String, Object>> dto = new ArrayList<>();

        game.getGamePlayers().forEach(gamePlayer -> dto.addAll(makeSalvoesListaDTO(gamePlayer.getSalvoes())));

        return dto;
    }

    //Desde game necesito todos los gameplayers, y desde gameplayer necesito todos los salvoes
    //Lista de mapas.
    public List<Map<String, Object>> makeSalvoesListaDTO(Set<Salvo> salvoes) {
        return salvoes
                .stream()
                .map(salvo -> makeSalvoDTO(salvo))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo) {

        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getId());
        dto.put("locations", salvo.getLocations());

        return dto;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<String> createUser(
            @RequestParam String userName,
            @RequestParam String email, @RequestParam String password) {

        if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByEmail(email) !=  null) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(userName, email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);
        if(authenticatedPlayer == null){
            return new ResponseEntity<>(makeMap("error","No name given"), HttpStatus.FORBIDDEN);
        } else {
            Date date = Date.from(java.time.ZonedDateTime.now().toInstant());
            Game auxGame = new Game(date);
            gameRepository.save(auxGame);

            GamePlayer auxGameP = new GamePlayer(auxGame,authenticatedPlayer);
            gamePlayerRepository.save(auxGameP);
            return new ResponseEntity<>(makeMap("gpid", auxGameP.getId()), HttpStatus.CREATED);
        }
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
