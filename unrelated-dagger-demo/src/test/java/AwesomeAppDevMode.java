import dagger.Component;

import javax.inject.Singleton;

@Component(modules = {
        AwesomeAppModule.class,
        H2JooqModule.class
})
@Singleton
interface AwesomeAppDevMode extends AwesomeApp {

    static void main(String[] args) {
        DaggerAwesomeAppDevMode
                .builder()
                .build()
                .app()
                .start();
    }
}