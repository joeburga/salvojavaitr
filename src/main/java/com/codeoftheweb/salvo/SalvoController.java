package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    /* ######################## @Autowireds ######################## */

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private SalvoRepository salvoRepository;


    /* ######################## MÉTODOS AUXILIARES ######################## */

    /**
     * Verifica que el Player sea el mismo del Game que el authenticatedPlayer.
     *
     * @param gamePlayer
     * @param authenticatedPlayer
     * @return boolean
     */
    private boolean correctGamePlayer(GamePlayer gamePlayer, Player authenticatedPlayer) {
        return gamePlayer.getPlayer().getId() == authenticatedPlayer.getId();
    }

    /**
     * Para las ResponseEntity
     *
     * @param key
     * @param value
     * @return Map<String, Object>
     */
    private Map<String, Object> responseInfo(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Obtengo el Player que esta logueado en el Juego.
     *
     * @param authentication
     * @return Player
     */
    private Player getAuthenticationPlayer(Authentication authentication) {

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        } else {
            return playerRepository.findByEmail(authentication.getName());
        }
    }

    /**
     * Obtengo el Oponente de mi Juego.
     *
     * @param gamePlayer
     * @return GamePlayer
     */
    private GamePlayer getOpponent(GamePlayer gamePlayer) {
        return gamePlayer.getGame().getGamePlayers().stream()
                .filter(gp -> gp != gamePlayer)
                .findFirst()
                .orElse(null);
    }

    /* ######################## @RequestMapping - GETs - FOR DTOs ######################## */

    @RequestMapping("/games")
    public Map<String, Object> makeAuthenticatedPlayer(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);

        if (authenticatedPlayer == null)
            dto.put("player", "Guest");
        else
            dto.put("player", makePlayerDTO(authenticatedPlayer));


        dto.put("games", getAllGamePlayers());

        return dto;
    }

    @RequestMapping("/game_view/{id}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable Long id,
                                                           Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).get();

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "You need to be logged first"), HttpStatus.FORBIDDEN);
        }
        if (!correctGamePlayer(gamePlayer, authenticatedPlayer)) {
            return new ResponseEntity<>(responseInfo("error", "Wrong gamePlayerID"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getGame().getGamePlayers().size() == 1) {
            return new ResponseEntity<>(responseInfo("error", "Waiting for next player, go back homePage!!"), HttpStatus.UNAUTHORIZED);
        }

        dto = gameViewDTO(gamePlayer);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping("/leaderBoard")
    public List<Object> makeLeaderBoard() {
        return playerRepository
                .findAll()
                .stream()
                .map(player -> makePlayerDTO(player))
                .collect(Collectors.toList());
    }


    /* ######################## DTO GAME_VIEW ######################## */

    private Map<String, Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();

        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getDate());
        // Estoy en GamePlayer, invoco a getGame() para obtener el Game y traerme los GamePlayers.
        dto.put("gamePlayers", makeGamePlayersList(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", makeShipsList(gamePlayer.getShips()));
        dto.put("salvoes", makeGamesSalvoesList(gamePlayer.getGame()));
        dto.put("scores", makeGamesScoresList(gamePlayer.getGame().getScores()));
        dto.put("gameState", getGameState(gamePlayer,getOpponent(gamePlayer)));
        dto.put("hits", hitsDTO(gamePlayer, getOpponent(gamePlayer)));

        return dto;
    }

    private String getGameState(GamePlayer self, GamePlayer opponent){

        if (self.getShips().size() == 0){
            return "PLACESHIPS";
        }
        if (opponent == null || opponent.getShips().size() == 0){
            return "WAITINGFOROPP";
        }

        if (self.getShips().size() > 4 && opponent.getShips().size() > 4){
            return "ENTER SALVO";
        } else {
            return "PLAY";
        }

/*        if (allShipsSunk(self.getShips(),opponent.getSalvoes())){

            if (allShipsSunk(opponent.getShips(),self.getSalvoes())){
                return "TIE";
            } else {
                return "LOST";
            }
        } else {

            return "WON";
        }*/

    }

    private boolean allShipsSunk(Set<Ship> ships, Set<Salvo> salvoes){

        List<String> salvoesLocationsList = salvoes.stream().map(salvo -> salvo.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        List<String> shipsLocationsList = ships.stream().map(ship -> ship.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());

        int longCarrier = 0;
        int longBattleship = 0;
        int longDestroyer = 0;
        int longSubmarine = 0;
        int longPatrolBoat = 0;

        for (Ship ship : ships) {
            switch (ship.getType()) {
                case "Carrier":
                    longCarrier = 5;
                    break;
                case "Battleship":
                    longBattleship = 4;
                    break;
                case "Destroyer":
                    longDestroyer = 3;
                    break;
                case "Submarine":
                    longSubmarine = 3;
                    break;
                case "PatrolBoat":
                    longPatrolBoat = 2;
                    break;
            }
        }

        return true ;
    }

    public int currentTurn(GamePlayer self, GamePlayer opponent){

        List<Salvo> selfSalvoesList = self.getSalvoes().stream().collect(Collectors.toList());
        List<Salvo> opponentSalvoesList = opponent.getSalvoes().stream().collect(Collectors.toList());

        List<Salvo> allSalvoes = new ArrayList<>();

        allSalvoes.addAll(selfSalvoesList);
        allSalvoes.addAll(opponentSalvoesList);

        return allSalvoes.stream().mapToInt(salvo -> salvo.getTurn()).max().getAsInt();

    }


    private Map<String, Object> hitsDTO(GamePlayer self, GamePlayer opponent) {
        Map<String, Object> dto = new LinkedHashMap<>();

        dto.put("self", getHits(self, opponent));
        dto.put("opponent", getHits(opponent, self));

        return dto;
    }

    private List<Map> getHits(GamePlayer self, GamePlayer opponent) {
        List<Map> dto = new ArrayList<>();

        int selfCarrierDamage = 0;
        int selfDestroyerDamage = 0;
        int selfPatrolboatDamage = 0;
        int selfSubmarineDamage = 0;
        int selfBattleshipDamage = 0;

        List<String> selfCarrierLocations = new ArrayList<>();
        List<String> selfDestroyerLocations = new ArrayList<>();
        List<String> selfSubmarineLocations = new ArrayList<>();
        List<String> selfPatrolboatLocations = new ArrayList<>();
        List<String> selfBattleshipLocations = new ArrayList<>();

        for (Ship selfShip : self.getShips()) {
            switch (selfShip.getType()) {
                case "Carrier":
                    selfCarrierLocations = selfShip.getLocations();
                    break;
                case "Battleship":
                    selfBattleshipLocations = selfShip.getLocations();
                    break;
                case "Destroyer":
                    selfDestroyerLocations = selfShip.getLocations();
                    break;
                case "Submarine":
                    selfSubmarineLocations = selfShip.getLocations();
                    break;
                case "PatrolBoat":
                    selfPatrolboatLocations = selfShip.getLocations();
                    break;
            }
        }

        // Ordeno los Salvos por Turno para que el JSON esté ordenado.
        List<Salvo> opponentSalvoes = opponent.getSalvoes()
                .stream()
                .sorted(Comparator.comparingInt(Salvo::getTurn))
                .collect(Collectors.toList());

        for (Salvo opponentSalvo : opponentSalvoes) {
            Integer carrierHitsInTurn = 0;
            Integer battleshipHitsInTurn = 0;
            Integer submarineHitsInTurn = 0;
            Integer destroyerHitsInTurn = 0;
            Integer patrolboatHitsInTurn = 0;
            Integer missedShots = opponentSalvo.getLocations().size();

            Map<String, Object> hitsMapPerTurn = new LinkedHashMap<>();
            Map<String, Object> damagesPerTurn = new LinkedHashMap<>();
            List<String> opponentSalvoLocationsList = new ArrayList<>();
            List<String> hitCellsList = new ArrayList<>();

            opponentSalvoLocationsList.addAll(opponentSalvo.getLocations());

            for (String opponentSalvoShot : opponentSalvoLocationsList) {
                if (selfCarrierLocations.contains(opponentSalvoShot)) {
                    selfCarrierDamage++;
                    carrierHitsInTurn++;
                    hitCellsList.add(opponentSalvoShot);
                    missedShots--;
                }
                if (selfBattleshipLocations.contains(opponentSalvoShot)) {
                    selfBattleshipDamage++;
                    battleshipHitsInTurn++;
                    hitCellsList.add(opponentSalvoShot);
                    missedShots--;
                }
                if (selfSubmarineLocations.contains(opponentSalvoShot)) {
                    selfSubmarineDamage++;
                    submarineHitsInTurn++;
                    hitCellsList.add(opponentSalvoShot);
                    missedShots--;
                }
                if (selfDestroyerLocations.contains(opponentSalvoShot)) {
                    selfDestroyerDamage++;
                    destroyerHitsInTurn++;
                    hitCellsList.add(opponentSalvoShot);
                    missedShots--;
                }
                if (selfPatrolboatLocations.contains(opponentSalvoShot)) {
                    selfPatrolboatDamage++;
                    patrolboatHitsInTurn++;
                    hitCellsList.add(opponentSalvoShot);
                    missedShots--;
                }
            }

            hitsMapPerTurn.put("currentTurn", currentTurn(self, opponent));

            hitsMapPerTurn.put("turn", opponentSalvo.getTurn());
            hitsMapPerTurn.put("hitLocations", hitCellsList);
            hitsMapPerTurn.put("damages", damagesPerTurn);
            hitsMapPerTurn.put("missed", missedShots);

            damagesPerTurn.put("carrierHits", carrierHitsInTurn);
            damagesPerTurn.put("battleshipHits", battleshipHitsInTurn);
            damagesPerTurn.put("submarineHits", submarineHitsInTurn);
            damagesPerTurn.put("destroyerHits", destroyerHitsInTurn);
            damagesPerTurn.put("patrolboatHits", patrolboatHitsInTurn);
            damagesPerTurn.put("carrier", selfCarrierDamage);
            damagesPerTurn.put("battleship", selfBattleshipDamage);
            damagesPerTurn.put("submarine", selfSubmarineDamage);
            damagesPerTurn.put("destroyer", selfDestroyerDamage);
            damagesPerTurn.put("patrolboat", selfPatrolboatDamage);

            dto.add(hitsMapPerTurn);
        }

        return dto;
    }

    /* ######################## DTO GAMES ######################## */

    public List<Object> getAllGamePlayers() {
        return gameRepository
                .findAll()
                .stream()
                .map(game -> makeGameDTO(game))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("id", game.getId());
        dto.put("creationDate", game.getDate());
        dto.put("gamePlayers", makeGamePlayersList(game.getGamePlayers()));
        dto.put("scores", makeGamesScoresList(game.getScores()));

        return dto;
    }


    /* ######################## DTO GAMEPLAYERS ######################## */

    public List<Object> makeGamePlayersList(Set<GamePlayer> gamePlayers) {
        return gamePlayers
                .stream()
                // Ordeno por GamePlayerID para que el JSON esté ordenado.
                .sorted(Comparator.comparingLong(GamePlayer::getId))
                .map(gamePlayer -> makeGamePlayersDTO(gamePlayer))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGamePlayersDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("gpid", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));

        return dto;
    }

    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("id", player.getId());
        // Agregue un campo username.
        dto.put("username", player.getUserName());
        dto.put("email", player.getEmail());
        dto.put("score", makeScoresListDTO(player));

        return dto;
    }


    /* ######################## DTO SHIPS ######################## */

    public List<Object> makeShipsList(Set<Ship> ships) {
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


    /* ######################## DTO SALVOES ######################## */

    /*
        LISTA DE MAPAS.
        Desde game necesito todos los gameplayers, y desde gameplayer necesito todos los salvoes.
    */
    private List<Map<String, Object>> makeGamesSalvoesList(Game game) {
        List<Map<String, Object>> dto = new ArrayList<>();

        game.getGamePlayers().forEach(gamePlayer -> dto.addAll(makeSalvoesList(gamePlayer.getSalvoes())));

        return dto;
    }

    public List<Map<String, Object>> makeSalvoesList(Set<Salvo> salvoes) {
        return salvoes
                .stream()
                // Ordeno por Turno para que el JSON esté ordenado.
                .sorted(Comparator.comparingInt(Salvo::getTurn))
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


    /* ######################## DTO SCORES ######################## */

    private Map<String, Object> makeScoresListDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("name", player.getUserName());
        dto.put("total", player.getScore(player));
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost", player.getLosses(player.getScores()));
        dto.put("tied", player.getDraws(player.getScores()));

        return dto;
    }

    public List<Object> makeGamesScoresList(Set<Score> scores) {
        return scores
                .stream()
                //Ordeno por ScoreID
                .sorted(Comparator.comparingLong(Score::getId))
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


    /* ######################## @RequestMapping - POSTs ######################## */

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createUser(@RequestParam String userName,
                                                          @RequestParam String email,
                                                          @RequestParam String password) {

        // 1.- Si los campos del sign up estan vacios, envio respuesta FORBIDDEN.
        // 2.- Si ya hay un Player con el mismo email, envio respuesta FORBIDDEN.
        // 3.- Creo un newPlayer con el userName, email y password.
        //     Guardo newPlayer en su respectivo repositorio.
        //     Envio respuesta CREATED.

        if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(responseInfo("error", "Missing data"), HttpStatus.FORBIDDEN);
        }
        if (playerRepository.findByEmail(email) != null) {
            return new ResponseEntity<>(responseInfo("error", "Name already in use"), HttpStatus.FORBIDDEN);
        }

        Player newPlayer = new Player(userName, email, passwordEncoder.encode(password));
        playerRepository.save(newPlayer);

        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);

        // 1.- Si no hay Player logueado envio respuesta FORBIDDEN.
        // 2.- Si hay Player logueado.
        //      Creo un Game y lo guardo en su respectivo repositorio.
        //      Creo un GamePlayer, lo vinculo con Game y Player, y lo guardo en su respectivo repositorio.
        //      Envio respuesta CREATED.

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "You need to be logged first"), HttpStatus.FORBIDDEN);
        } else {

            Date date = Date.from(java.time.ZonedDateTime.now().toInstant());
            Game newGame = new Game(date);
            gameRepository.save(newGame);

            GamePlayer newGamePlayer = new GamePlayer(newGame, authenticatedPlayer);
            gamePlayerRepository.save(newGamePlayer);

            return new ResponseEntity<>(responseInfo("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/game/{nn}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long nn,
                                                        Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);
        Game game = gameRepository.findById(nn).orElse(null);

        // 1.- Si no hay Player logueado envio respuesta UNAUTHORIZED.
        // 2.- Si no hay Game con el Id(nn) envio respuesta FORBIDDEN.
        // 3.- Si en Game hay más de 2 jugadores envio respuesta FORBIDDEN.
        // 4.- Creo un newGamePlayer con el Game y el Player.
        //     Vinculo el Game con el newGamePlayer.
        //     Guardo el newGamePlayer en su respectivo respositorio.

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "You need to be logged first"), HttpStatus.UNAUTHORIZED);
        }
        if (game == null) {
            return new ResponseEntity<>(responseInfo("error", "No such game"), HttpStatus.FORBIDDEN);
        }
        if (game.getGamePlayers().size() >= 2) {
            return new ResponseEntity<>(responseInfo("error", "Game is full"), HttpStatus.FORBIDDEN);
        }

        GamePlayer newGamePlayer = new GamePlayer(game, authenticatedPlayer);
        game.addGame(newGamePlayer);
        gamePlayerRepository.save(newGamePlayer);

        return new ResponseEntity<>(responseInfo("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addShips(@PathVariable Long gamePlayerId,
                                                        @RequestBody Set<Ship> ships, //Cuerpo de la Request
                                                        Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);

        // 1.- Si no hay Player logueado envio respuesta UNAUTHORIZED.
        // 2.- Si no hay GamePlayer con el Id(gamePlayerId) envio respuesta UNAUTHORIZED.
        // 3.- Verifico que GamePlayer sea el mismo del authenticatedPlayer.
        //  3.1.- Verifico si el GamePlayer ya tiene vinculados sus Ships.
        //        Si no los tiene, vinculo Ships con GamePlayer.
        //        Guardo los Ships en su respectivo respositorio.
        //        Envio respuesta CREATED.
        //  3.2.- Si los tiene, envio respuesta FORBIDDEN.
        // 4.- Si no lo es, envio respuesta UNAUTHORIZED.

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "You need to be logged first"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "No such gamePlayer"), HttpStatus.UNAUTHORIZED);
        }
        if (correctGamePlayer(gamePlayer, authenticatedPlayer)) {
            if (gamePlayer.getShips().isEmpty()) {
                ships.forEach(ship -> ship.setGamePlayer(gamePlayer));
                gamePlayer.setShips(ships);
                shipRepository.saveAll(ships);

                return new ResponseEntity<>(responseInfo("ok", "Ships saved"), HttpStatus.CREATED);

            } else {
                return new ResponseEntity<>(responseInfo("error", "Player already has ships"), HttpStatus.FORBIDDEN);
            }

        } else {
            return new ResponseEntity<>(responseInfo("error", "Wrong gamePlayerID"), HttpStatus.UNAUTHORIZED);

        }
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addSalvoes(@PathVariable Long gamePlayerId,
                                                          @RequestBody Salvo salvo,
                                                          Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);

        // 1.- Si no hay Player logueado envio respuesta UNAUTHORIZED.
        // 2.- Si no hay GamePlayer con el Id(gamePlayerId) envio respuesta UNAUTHORIZED.
        // 3.- Verifico que GamePlayer sea el mismo del authenticatedPlayer.
        //  3.1.- Verifico si el GamePlayer ya ha disparado su Salvo.
        //        Si ha disparado, envio respuesta FORBIDDEN.
        //  3.2.- Si no ha disparado, vinculo Salvo con GamePlayer.
        //        Guardo el Salvo en su respectivo respositorio.
        //        Envio respuesta CREATED.
        // 4.- Si no lo es, envio respuesta UNAUTHORIZED.

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "You need to be logged first"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "No gamePlayerID given"), HttpStatus.UNAUTHORIZED);
        }
        if (correctGamePlayer(gamePlayer, authenticatedPlayer)) {
            // Hago un filter y count para saber si el Turn no esta repetido.
            if (gamePlayer.getSalvoes().stream().filter(s -> s.getTurn() == salvo.getTurn()).count() == 1) {
                return new ResponseEntity<>(responseInfo("error", "Player already has the salvo"), HttpStatus.FORBIDDEN);

            } else {
                //gamePlayer.getSalvoes().stream().filter(s -> s.getTurn() == salvo.getTurn()).collect(Collectors.counting()) + 1;
                gamePlayer.getSalvoes().add(salvo);
                salvoRepository.save(salvo);

                return new ResponseEntity<>(responseInfo("ok", "Salvoes saved"), HttpStatus.CREATED);
            }

        } else {
            return new ResponseEntity<>(responseInfo("error", "Wrong gamePlayerID"), HttpStatus.UNAUTHORIZED);

        }
    }
}
