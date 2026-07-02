package backend.model;

public class GameEvent {
    
    private String type;
    private String message;
    private long timestamp;
    private String playerName;
    
    public GameEvent(String type, String message, String playerName) {
        this.type = type;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.playerName = playerName;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
