package com.example.springbootusers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.google.cloud.spanner.TransactionRunner.TransactionCallable;
import static com.google.cloud.spanner.Type.StructField;

import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Operation;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.Struct;
import com.google.cloud.spanner.TimestampBound;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.Type;
import com.google.cloud.spanner.Value;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;
import com.google.spanner.admin.database.v1.UpdateDatabaseDdlMetadata;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SpringBootUsersApplication {

    /** Class to contain user sample data. */
    static class User {

        String username;
        String password;

        User(String username, String password){
            this.username = username;
            this.password = password;
        }
    }

    // [START spanner_insert_data]
    static final List<User> USERS =
            Arrays.asList(
                    new User("1111", "2221"),
                    new User("1112", "2222"),
                    new User("1113", "2223"),
                    new User("1114", "2224"),
                    new User("1115", "2225"));
    // [END spanner_insert_data]

    // [START spanner_create_database]
    static void createDatabase(DatabaseAdminClient dbAdminClient, DatabaseId id) {
        Operation<Database, CreateDatabaseMetadata> op =
                dbAdminClient.createDatabase(
                        id.getInstanceId().getInstance(),
                        id.getDatabase(),
                        Arrays.asList(
                                "CREATE TABLE Users (\n"
                                        + "  Username   STRING(1024),\n"
                                        + "  Password   STRING(1024),\n"
                                        + ") PRIMARY KEY (SingerId)"));
        Database db = op.waitFor().getResult();
        System.out.println("Created database [" + db.getId() + "]");
    }
    // [END spanner_create_database]

    // [START spanner_insert_data_with_timestamp_column]
    static void writeExampleData(DatabaseClient dbClient) {
        List<Mutation> mutations = new ArrayList<Mutation>();
        for (User user : USERS) {
            mutations.add(
                    Mutation.newInsertBuilder("Users")
                            .set("Username")
                            .to(user.username)
                            .set("Password")
                            .to(user.password)
                            .build());
        }
        dbClient.write(mutations);
    }
    // [END spanner_insert_data_with_timestamp_column]

    // [START spanner_query_data]
    static void query(DatabaseClient dbClient) {
        // singleUse() can be used to execute a single read or query against Cloud Spanner.
        ResultSet resultSet =
                dbClient
                        .singleUse()
                        .executeQuery(Statement.of("SELECT Username FROM Users"));
        while (resultSet.next()) {
            System.out.printf(
                    "%s %s\n", resultSet.getString(0), resultSet.getString(1));
        }
    }
    // [END spanner_query_data]

    // [START spanner_read_data]
    static void read(DatabaseClient dbClient) {
        ResultSet resultSet =
                dbClient
                        .singleUse()
                        .read(
                                "Users",
                                // KeySet.all() can be used to read all rows in a table. KeySet exposes other
                                // methods to read only a subset of the table.
                                KeySet.all(),
                                Arrays.asList("Username", "Password"));
        while (resultSet.next()) {
            System.out.printf(
                    "%s %s\n", resultSet.getString(0), resultSet.getString(1));
        }
    }
    // [END spanner_read_data]

    /* *
    static void run(
            DatabaseClient dbClient,
            DatabaseAdminClient dbAdminClient,
            String command,
            DatabaseId database) {
        switch (command) {
            case "createdatabase":
                createDatabase(dbAdminClient, database);
                break;
            case "write":
                writeExampleData(dbClient);
                break;
            case "query":
                query(dbClient);
                break;
            case "read":
                read(dbClient);
                break;
        }
    }
    * */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootUsersApplication.class, args);
        if (args.length != 2) {
            return;
        }
        // [START init_client]
        SpannerOptions options = SpannerOptions.newBuilder().build();
        Spanner spanner = options.getService();


        try {
            String instanceId = args[0];
            String databaseId = args[1];
            // Creates a database client
            DatabaseClient dbClient = spanner.getDatabaseClient(DatabaseId.of(options.getProjectId(), instanceId, databaseId));
            DatabaseAdminClient dbAdminClient = spanner.getDatabaseAdminClient();
            DatabaseId db = DatabaseId.of(options.getProjectId(), args[0], args[1]);
            // [END init_client]
            createDatabase(dbAdminClient, db);
            writeExampleData(dbClient);
            read(dbClient);
            query(dbClient);
        } finally {
            spanner.close();
        }
        System.out.println("Closed client");
    }
}
