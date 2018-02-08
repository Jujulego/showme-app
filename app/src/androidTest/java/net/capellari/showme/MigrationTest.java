package net.capellari.showme;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.capellari.showme.db.AppDatabase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by julien on 08/02/18.
 *
 * Test des migrations
 */

@RunWith(AndroidJUnit4.class)
public class MigrationTest {
    // Constantes
    private static final String TEST_APPDB = "migration-test-app";

    // Règles
    @Rule
    public MigrationTestHelper appHelper;

    // Constructeur
    public MigrationTest() {
        appHelper = new MigrationTestHelper(
                InstrumentationRegistry.getInstrumentation(),
                AppDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory()
        );
    }

    // Tests
    @Test
    public void migrateApp1To2() throws IOException {
        SupportSQLiteDatabase db = appHelper.createDatabase(TEST_APPDB, 1);

        // Un peu de données
        db.execSQL("INSERT INTO Type VALUES (1, 'test', 'tests')");
        db.close();

        // Application de la migration
        db = appHelper.runMigrationsAndValidate(TEST_APPDB, 2, true, AppDatabase.MIGRATION_1_2);

        // Check données
        Cursor cursor = db.query("SELECT * FROM Type");

        assertThat(cursor.getCount(), equalTo(1));

        cursor.moveToFirst();
        assertThat(cursor.getString(cursor.getColumnIndex("nom")), equalTo("test"));
        assertThat(cursor.getInt(cursor.getColumnIndex("blacklist")), equalTo(0));
    }
}
