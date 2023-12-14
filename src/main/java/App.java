import org.generator.UniqueIdGenerator;

import java.time.Clock;

public class App {

    public static void main(String[] args) {
        UniqueIdGenerator generator = null;
        if (args.length == 1) {
            generator = UniqueIdGenerator.create(Clock.systemUTC(), args[0]);
        } else {
            generator = UniqueIdGenerator.create();
        }
        var id = generator.generate();
        System.out.println(id);
    }

}
