import dagger.Module;
import dagger.Provides;
import io.javalin.Javalin;

import javax.inject.Singleton;

@Module
class AwesomeAppModule {

    @Provides
    @Singleton
    Javalin provideApp() {
        Javalin app = Javalin.create();
        app.post("/health", web -> {
            web.result("OK");
        });
        return app;
    }


}