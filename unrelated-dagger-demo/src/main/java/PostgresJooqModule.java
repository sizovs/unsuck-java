import dagger.Module;
import dagger.Provides;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.ThreadLocalTransactionProvider;
import org.postgresql.ds.PGSimpleDataSource;

import javax.inject.Singleton;

@Module
public class PostgresJooqModule {

    @Provides
    @Singleton
    static DSLContext providesDSLContext() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://some.real.ip/app");
        var txProvider = new ThreadLocalTransactionProvider(new DataSourceConnectionProvider(dataSource));
        var dsl = DSL.using(dataSource, SQLDialect.POSTGRES);
        dsl.configuration().set(txProvider);
        return dsl;
    }

}