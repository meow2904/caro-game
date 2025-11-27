package com.myproject.caro_game.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.myproject.caro_game.models.Game;
import com.myproject.caro_game.models.Move;
import com.myproject.caro_game.models.Player;

@Service
public class GameService {
    private final Map<String, Game> games = new HashMap<>();

    public String createRoom(String playerName) {
        String zoomId = UUID.randomUUID().toString();
        Game game = new Game();
        Player player1 = new Player(playerName, 'X');
        game.addPlayer(player1);
        games.put(zoomId, game);
        return zoomId;
    }

    public Game getGame(String zoomId) {
        return games.get(zoomId);
    }

    public Player addPlayerToRoom(String zoomId, String playerName) {
        Game game = games.get(zoomId);
        if (game == null) return null;
        Player player2 = new Player(playerName, 'O');
        game.addPlayer(player2);
        return player2;
    }

    public boolean placeMove(String zoomId, Move move) {
        Game game = games.get(zoomId);
        if (game == null) return false;
        Player current = game.getCurrentPlayer();
        if (!current.getUserId().equals(move.userId())) return false;

        boolean success = game.getBoard().placeMove(move.point(), move.symbol());
        if (success) game.switchPlayer();
        return success;
    }

    // /**
    //  * Kiểm tra thắng cơ bản 5 ô liên tiếp
    //  */
    // private boolean checkWin(int x, int y, char symbol) {
    //     return countContinuous(x, y, symbol, 1, 0) + countContinuous(x, y, symbol, -1, 0) - 1 >= 5 // ngang
    //             || countContinuous(x, y, symbol, 0, 1) + countContinuous(x, y, symbol, 0, -1) - 1 >= 5 // dọc
    //             || countContinuous(x, y, symbol, 1, 1) + countContinuous(x, y, symbol, -1, -1) - 1 >= 5 // chéo \
    //             || countContinuous(x, y, symbol, 1, -1) + countContinuous(x, y, symbol, -1, 1) - 1 >= 5; // chéo /
    // }

    // /**
    //  * Đếm số ô liên tiếp theo hướng dx, dy
    //  */
    // private int countContinuous(int x, int y, char symbol, int dx, int dy) {
    //     int count = 0;
    //     int nx = x, ny = y;
    //     while (true) {
    //         Character c = game.getBoard().getSymbolAt(nx, ny);
    //         if (c != null && c == symbol) {
    //             count++;
    //             nx += dx;
    //             ny += dy;
    //         } else {
    //             break;
    //         }
    //     }
    //     return count;
    // }
}
