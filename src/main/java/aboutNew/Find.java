package aboutNew;
import io.javalin.Javalin;

public class Find {

	public static String world() {
		 String w =  "World";
		 return w;
	}
	
	public static void main(String[] args) {
        var app = Javalin.create(/*config*/)
            .get("/", ctx -> ctx.result("Hello World"))
            .start(7070);
    }
	

}
