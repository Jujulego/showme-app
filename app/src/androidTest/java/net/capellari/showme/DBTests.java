package net.capellari.showme;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.Type;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * DB test
 */
@RunWith(AndroidJUnit4.class)
public class DBTests {
    // Attributs
    private Type.TypeDAO m_typeDAO;
    private AppDatabase m_db;

    @Before
    public void createDB() {
        Context context = InstrumentationRegistry.getTargetContext();

        m_db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        m_typeDAO = m_db.getTypeDAO();
    }

    @Test
    public void insertType() {
        Type type = new Type();
        type._id = 5;
        type.ordre = 2;
        type.nom = "cool !";

        m_typeDAO.insert(type);

        //List<Type> types = m_typeDAO.recupLive();
        //assertThat(types.get(0).nom, equalTo(type.nom));
    }

    @After
    public void closeDB() {
        m_db.close();
    }
}
