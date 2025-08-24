package aboutNew.model;

public class Tweet {

    private int id;
    private String username;
    private String content;
    private String date;

    public Tweet(String username, String content, String date) 
    {
        this.username = username;
        this.content = content;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    
}
