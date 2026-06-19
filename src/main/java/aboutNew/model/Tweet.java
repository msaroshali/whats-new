package aboutNew.model;

public class Tweet {

    private int id;
    private String username;
    private String content;
    private String date;
    private String source;
    private float[] embedding;

    public Tweet(String username, String content, String date, String source) 
    {
        this.username = username;
        this.content = content;
        this.date = date;
        this.source = source;
    }

    public Tweet(String username, String content, String date, String source, float[] embedding) 
    {
        this.username = username;
        this.content = content;
        this.date = date;
        this.source = source;
        this.embedding = embedding;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public String getSource() { return source; }
    
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
    
}
