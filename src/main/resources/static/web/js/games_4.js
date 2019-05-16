$(function() {
  loadData();
  $("#login-btn").click(function(){
    login();
  });
  $("#logout-btn").click(function(){
    logout();
  });
});

function updateViewGames(data) {
  var userData = data.player;
  var htmlList = data.map(function (games) {
      return getGameItem(games,userData);
  }).join('');
  $("#game-list").html(htmlList);
  if(userData!="Guest"){
    $("#user-info").text('Hello ' + userData.name + '!');
    showLogin(false);
  }
}

function getGameItem(gameData, userData){
    console.log(gameData);
    var item = '<li class="list-group-item">'+ new Date(gameData.creationDate).toLocaleString() + ' ' + gameData.gamePlayers.map(function(p) { return p.player.email}).join(', ')  +'</li>';
    var idPlayerInGame = isPlayerInGame(userData.id,gameData);
    console.log(userData.id);

    if (idPlayerInGame != -1)
        item = '<li class="list-group-item"><a href="game_2.html?gp='+ idPlayerInGame + '">'+ new Date(gameData.creationDate).toLocaleString() + ' ' + gameData.gamePlayers.map(function(p) { return p.player.email}).join(', ')  +'</a></li>';
    return item;
}

function isPlayerInGame(idPlayer,gameData){
    var isPlayerInGame = -1;
    gameData.gamePlayers.forEach(function (game){
        if(idPlayer === game.player.id)
            isPlayerInGame = game.id;
    });
    return isPlayerInGame;
}

function updateViewLBoard(data) {
  var htmlList = data.map(function (score) {
      return  '<tr><td>' + score.score.name + '</td>'
              + '<td>' + score.score.total + '</td>'
              + '<td>' + score.score.won + '</td>'
              + '<td>' + score.score.lost + '</td>'
              + '<td>' + score.score.tied + '</td></tr>';
  }).join('');
  document.getElementById("leader-list").innerHTML = htmlList;
}

function loadData() {
  $.get("/api/games")
    .done(function(data) {
      updateViewGames(data);
    })
    .fail(function( jqXHR, textStatus ) {
      alert( "Failed: " + textStatus );
    });
  
  $.get("/api/leaderBoard")
    .done(function(data) {
      updateViewLBoard(data);
    })
    .fail(function( jqXHR, textStatus ) {
      alert( "Failed: " + textStatus );
    });
}

function login(){
  $.post("/api/login", { email: $("#email").val(), password: $("#password").val()})
    .done(function() {
      loadData(),
      showLogin(false);
    });
    //$.post("/api/login", { email: "j.bauer@ctu.gov", password: "123" }).done(function() { console.log("logged in!"); })
}

function logout(){
  $.post("/api/logout")
    .done(function() {
      loadData();
      showLogin(true);
    });
    //$.post("/api/logout").done(function() { console.log("logged out"); })
}

function showLogin(show){
  if(show){
    $("#login-panel").show();
    $("#user-panel").hide();
  } else {
    $("#login-panel").hide();
    $("#user-panel").show();
  }
}