package aboutNew.model;

public class Tweet {

    private int id;
    private String username;
    private String content;
    private String date;
    private String source;

    public Tweet(String username, String content, String date, String source) 
    {
        this.username = username;
        this.content = content;
        this.date = date;
        this.source = source;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public String getSource() { return source; }
    
}
