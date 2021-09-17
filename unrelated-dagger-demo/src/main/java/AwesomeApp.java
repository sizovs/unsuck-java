import dagger.Component;
import io.javalin.Javalin;

import javax.inject.Singleton;

@Component(modules = {
        AwesomeAppModule.class,
        PostgresJooqModule.class
})
@Singleton
public interface AwesomeApp {

    Javalin app();

    static void main(String[] args) {
        DaggerAwesomeApp
                .builder()
                .build()
                .app()
                .start();
    }

}