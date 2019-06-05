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
    @Autowired
    private ScoreRepository scoreRepository;

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
                .orElse(new GamePlayer());
    }

    /**
     * Obtengo la longitud del tipo de Ship.
     *
     * @param ship
     * @return int
     */
    private int longShip(Ship ship) {

        int longShipType;

        switch (ship.getType()) {
            case "carrier":
                longShipType = 5;
                break;
            case "battleship":
                longShipType = 4;
                break;
            case "destroyer":
                longShipType = 3;
                break;
            case "submarine":
                longShipType = 3;
                break;
            case "patrolboat":
                longShipType = 2;
                break;
            default:
                longShipType = 0;
        }
        return longShipType;
    }

    /**
     * Obtengo el Turno actual del Game.
     *
     * @param self
     * @param opponent
     * @return int
     */
    private int currentTurnGame(GamePlayer self, GamePlayer opponent) {

        int selfGPSalvoes = self.getSalvoes().size();
        int opponentGPSalvoes = opponent.getSalvoes().size();

        int totalSalvoes = selfGPSalvoes + opponentGPSalvoes;

        if (totalSalvoes % 2 == 0)
            return totalSalvoes / 2 + 1;

        return (int) (totalSalvoes / 2.0 + 0.5);
    }

    /**
     * Verifica si ya existe el Score de un Player en el Game.
     *
     * @param score
     * @param game
     * @return boolean
     */
    private boolean existScore(Score score, Game game) {
        Set<Score> scores = game.getScores();

        for (Score s : scores) {
            if (score.getPlayer().getEmail().equals(s.getPlayer().getEmail())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si todos mis Ships se hundieron.
     *
     * @param selfShips
     * @param opponentSalvoes
     * @return boolean
     */
    private boolean allShipsSunk(Set<Ship> selfShips, Set<Salvo> opponentSalvoes) {
        Map<String, Object> damages = getDamages(selfShips, opponentSalvoes);

        long selfSunkenShips = selfShips
                .stream()
                .filter(ship -> Long.parseLong(String.valueOf(damages.get(ship.getType()))) == longShip(ship))
                .count();

        return selfSunkenShips == 5;
    }

    /**
     * Verifica si en un Turn ya ha disparado un Salvo.
     *
     * @param newSalvo
     * @param playerSalvoes
     * @return boolean
     */
    private boolean turnHasSalvoes(Salvo newSalvo, Set<Salvo> playerSalvoes) {
        boolean hasSalvoes = false;
        for (Salvo salvo : playerSalvoes) {
            if (salvo.getTurn() == newSalvo.getTurn()) {
                hasSalvoes = true;
            }
        }
        return hasSalvoes;
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
        }/*
        if (gamePlayer.getGame().getGamePlayers().size() == 1) {
            return new ResponseEntity<>(responseInfo("error", "Waiting for next player, go back homePage!!"), HttpStatus.UNAUTHORIZED);
        }
*/
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

        GamePlayer opponent = getOpponent(gamePlayer);

        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getDate());
        // Estoy en GamePlayer, invoco a getGame() para obtener el Game y traerme los GamePlayers.
        dto.put("gamePlayers", makeGamePlayersList(gamePlayer.getGame().getGamePlayers()));

        if (gamePlayer.getShips().isEmpty()) {
            dto.put("ships", new ArrayList<>());
        } else {
            dto.put("ships", makeShipsList(gamePlayer.getShips()));
        }
        if (gamePlayer.getSalvoes().isEmpty()) {
            dto.put("salvoes", new ArrayList<>());
        } else {
            dto.put("salvoes", makeGamesSalvoesList(gamePlayer.getGame()));
        }

        dto.put("scores", makeGamesScoresList(gamePlayer.getGame().getScores()));
        dto.put("gameState", getGameState(gamePlayer, getOpponent(gamePlayer)));

        if (opponent == null) {
            dto.put("hits", emptyHitsDTO());

        } else {
            dto.put("hits", hitsDTO(gamePlayer, getOpponent(gamePlayer)));
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
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
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


    /* ######################## DTO GAME STATE ######################## */

    private String getGameState(GamePlayer self, GamePlayer opponent) {

        Set<Ship> selfShips = self.getShips();
        Set<Salvo> selfSalvoes = self.getSalvoes();

        Set<Ship> opponentShips = opponent.getShips();
        Set<Salvo> opponentSalvoes = opponent.getSalvoes();

        if (selfShips.size() == 0) {
            return "PLACE YOUR SHIPS";
        }
        if (opponentShips == null) {
            return "WAITING FOR OPPONENT SALVO";
        }
        if (opponentShips.size() == 0) {
            return "WAIT OPPONENT SHIPS";
        }
        if (selfSalvoes.size() == opponentSalvoes.size()) {
            Player selfPlayer = self.getPlayer();
            Game game = self.getGame();
            if (allShipsSunk(selfShips, opponentSalvoes) && allShipsSunk(opponentShips, selfSalvoes)) {
                Score score = new Score(game, selfPlayer, 0.5f, new Date());
                if (!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "TIE";
            }
            if (allShipsSunk(selfShips, opponentSalvoes)) {
                Score score = new Score(game, selfPlayer, 0, new Date());
                if (!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "LOST";
            }
            if (allShipsSunk(opponentShips, selfSalvoes)) {
                Score score = new Score(game, selfPlayer, 1, new Date());
                if (!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "WON";
            }
        }

        int currentTurn = currentTurnGame(self, opponent);

        if (selfSalvoes.size() != currentTurn) {
            return "PLAY";
        }

        return "WAITING FOR OPPONENT SALVO";
    }


    private Map<String, Object> getDamages(Set<Ship> selfShips, Set<Salvo> oppSalvoes) {
        Map<String, Object> dto = new LinkedHashMap<>();

        int carrierDamage = 0;
        int destroyerDamage = 0;
        int patrolboatDamage = 0;
        int submarineDamage = 0;
        int battleshipDamage = 0;
        List<String> carrierLocations = new ArrayList<>();
        List<String> destroyerLocations = new ArrayList<>();
        List<String> submarineLocations = new ArrayList<>();
        List<String> patrolboatLocations = new ArrayList<>();
        List<String> battleshipLocations = new ArrayList<>();

        for (Ship ship : selfShips) {
            switch (ship.getType()) {
                case "carrier":
                    carrierLocations = ship.getLocations();
                    break;
                case "battleship":
                    battleshipLocations = ship.getLocations();
                    break;
                case "destroyer":
                    destroyerLocations = ship.getLocations();
                    break;
                case "submarine":
                    submarineLocations = ship.getLocations();
                    break;
                case "patrolboat":
                    patrolboatLocations = ship.getLocations();
                    break;
            }
        }

        for (Salvo salvo : oppSalvoes) {
            List<String> salvoShot = new ArrayList<>();
            salvoShot.addAll(salvo.getLocations());

            for (String salvoLocation : salvoShot) {
                if (carrierLocations.contains(salvoLocation)) {
                    carrierDamage++;
                }
                if (battleshipLocations.contains(salvoLocation)) {
                    battleshipDamage++;
                }
                if (submarineLocations.contains(salvoLocation)) {
                    submarineDamage++;
                }
                if (destroyerLocations.contains(salvoLocation)) {
                    destroyerDamage++;
                }
                if (patrolboatLocations.contains(salvoLocation)) {
                    patrolboatDamage++;
                }
            }
        }

        dto.put("carrier", carrierDamage);
        dto.put("battleship", battleshipDamage);
        dto.put("submarine", submarineDamage);
        dto.put("destroyer", destroyerDamage);
        dto.put("patrolboat", patrolboatDamage);
        return dto;
    }


    /* ######################## DTO HITS ######################## */

    private Map<String, Object> emptyHitsDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();

        dto.put("self", new ArrayList<>());
        dto.put("opponent", new ArrayList<>());

        return dto;
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
                case "carrier":
                    selfCarrierLocations = selfShip.getLocations();
                    break;
                case "battleship":
                    selfBattleshipLocations = selfShip.getLocations();
                    break;
                case "destroyer":
                    selfDestroyerLocations = selfShip.getLocations();
                    break;
                case "submarine":
                    selfSubmarineLocations = selfShip.getLocations();
                    break;
                case "patrolboat":
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


    /* ######################## @RequestMapping - POSTs ######################## */

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createUser(@RequestParam String userName,
                                                          @RequestParam String email,
                                                          @RequestParam String password) {

        // 1.- Si los campos del sign up estan vacios, envio respuesta FORBIDDEN.
        // 2.- Si ya hay un Player con el mismo email, envio respuesta FORBIDDEN.
        // 3.- Si longitud email menor a 30 caracteres.
        //     Creo un newPlayer con el userName, email y password.
        //     Guardo newPlayer en su respectivo repositorio.
        //     Envio respuesta CREATED.
        // 4.- Si longitud email mayor a 30 caracteres envio respuesta FORBIDDEN.


        if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(responseInfo("error", "Missing data"), HttpStatus.FORBIDDEN);
        }
        if (playerRepository.findByEmail(email) != null) {
            return new ResponseEntity<>(responseInfo("error", "Name already in use"), HttpStatus.FORBIDDEN);
        }
        if (email.length() <= 30){
            Player newPlayer = new Player(userName, email, passwordEncoder.encode(password));
            playerRepository.save(newPlayer);

            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(responseInfo("error", "Please, type an email with a maximum of 30 characters"), HttpStatus.FORBIDDEN);
        }


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
        }

        Date date = Date.from(java.time.ZonedDateTime.now().toInstant());
        Game newGame = new Game(date);
        gameRepository.save(newGame);

        GamePlayer newGamePlayer = new GamePlayer(newGame, authenticatedPlayer);
        gamePlayerRepository.save(newGamePlayer);

        return new ResponseEntity<>(responseInfo("gpid", newGamePlayer.getId()), HttpStatus.CREATED);

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

                return new ResponseEntity<>(responseInfo("OK", "Ships Saved!!"), HttpStatus.CREATED);

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
            return new ResponseEntity<>(responseInfo("error", "No such gamePlayer"), HttpStatus.UNAUTHORIZED);
        }
        if (correctGamePlayer(gamePlayer, authenticatedPlayer)) {
            // Hago un filter y count para saber si el Turn no esta repetido.
            // gamePlayer.getSalvoes().stream().filter(s -> s.getTurn() == salvo.getTurn()).count() == 1
            if (turnHasSalvoes(salvo, gamePlayer.getSalvoes())) {
                return new ResponseEntity<>(responseInfo("error", "Player already has the salvo"), HttpStatus.FORBIDDEN);

            } else {

                salvo.setTurn(gamePlayer.getSalvoes().size() + 1);
                salvo.setGamePlayer(gamePlayer);

                gamePlayer.getSalvoes().add(salvo);
                salvoRepository.save(salvo);

                return new ResponseEntity<>(responseInfo("OK", "Salvoes Fired!!"), HttpStatus.CREATED);
            }

        } else {

            return new ResponseEntity<>(responseInfo("error", "Wrong gamePlayerID"), HttpStatus.UNAUTHORIZED);
        }
    }
}
