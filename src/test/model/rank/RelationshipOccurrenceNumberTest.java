package test.model.rank;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import jdbc.ConnectionFactory;

import model.Person;
import model.Relationship;
import model.RelationshipLink;
import model.rank.Popularity;
import model.rank.RelationshipOccurrenceNumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import control.Carla;
import control.HashMapData;

import test.setup.SetUp;
import dao.PersonDAO;
import dao.RelationshipDAO;

public class RelationshipOccurrenceNumberTest {
	
	private Connection connection;
	private double delta = 0.0000000000001d;

	@Before
	public void setUp() throws Exception {
		connection = new ConnectionFactory().getTestConnection();
		List<Person> persons = new ArrayList<>();
		this.cleanDatabase(connection);

		Person NasserOthmanRahman = new Person();
		NasserOthmanRahman.setName("Nasser Othman Rahman");
		persons.add(NasserOthmanRahman);

		Person LíberoRodrigues = new Person();
		LíberoRodrigues.setName("Líbero Rodrigues");
		persons.add(LíberoRodrigues);

		Person LeandroLunkesAmaral = new Person();
		LeandroLunkesAmaral.setName("Leandro Lunkes Amaral");
		persons.add(LeandroLunkesAmaral);

		PersonDAO pdao = new PersonDAO(connection);
		pdao.insert(persons);

		setRelationshipsNasser(connection);
	}

	private void cleanDatabase(Connection connection) throws SQLException {
		PreparedStatement stmt = null;
		String sql = "";

		sql = "delete from occurrence;";
		stmt = connection.prepareStatement(sql);
		stmt.execute();
		stmt.close();

		sql = "delete from relationship;";
		stmt = connection.prepareStatement(sql);
		stmt.execute();
		stmt.close();

		sql = "delete from person;";
		stmt = connection.prepareStatement(sql);
		stmt.execute();
		stmt.close();

		sql = "delete from page;";
		stmt = connection.prepareStatement(sql);
		stmt.execute();
		stmt.close();
	}

	/**
	 * Cria os relacionamentos para a pessoa Nasser Othman Rahman e a pessoa
	 * Líbero Rodrigues
	 */
	private void setRelationshipsNasser(Connection connection) {
		PersonDAO pdao = new PersonDAO(connection);
		Person nasser = pdao.getPerson("Nasser Othman Rahman").get(0);
		Person libero = pdao.getPerson("Líbero Rodrigues").get(0);
		Person leandro = pdao.getPerson("Leandro Lunkes Amaral").get(0);

		RelationshipDAO rdao = new RelationshipDAO(connection);
		Relationship relationship = new Relationship();

		// "http://www.google.com.br/"
		RelationshipLink rLink1 = new RelationshipLink();
		rLink1.setAverageDistance(0);
		rLink1.setLink(SetUp.GOOGLE);
		rLink1.setMaxDistance(0);
		rLink1.setMinDistance(0);
		rLink1.setOccurrenceNumber(5);
		relationship.addRelationshipLink(rLink1);

		// "http://pt.wikipedia.org/"
		RelationshipLink rLink2 = new RelationshipLink();
		rLink2.setAverageDistance(0);
		rLink2.setLink(SetUp.WIKIPEDIA);
		rLink2.setMaxDistance(0);
		rLink2.setMinDistance(0);
		rLink2.setOccurrenceNumber(3);
		relationship.addRelationshipLink(rLink2);

		// insere os relacionamentos no banco
		relationship.setPerson1(nasser);
		relationship.setPerson2(libero);
		rdao.insert(relationship);

		relationship = new Relationship();
		// "http://www.google.com.br/"
		rLink1 = new RelationshipLink();
		rLink1.setAverageDistance(0);
		rLink1.setLink(SetUp.GOOGLE);
		rLink1.setMaxDistance(0);
		rLink1.setMinDistance(0);
		rLink1.setOccurrenceNumber(5);
		relationship.addRelationshipLink(rLink1);

		// insere os relacionamentos no banco
		relationship.setPerson1(nasser);
		relationship.setPerson2(leandro);
		rdao.insert(relationship);
	}

	@After
	public void tearDown() throws Exception {
		// cleanDatabase(connection);
		connection.close();
	}

	/**
	 * D:\workspace_j2ee\Casual\data\tests\RelationshipOccurrenceNumberTest.jpg
	 */
	@Test
	public void testRelationshipOccurrenceNumber() {
		List<Relationship> relationships = null;
		try {
			HashMapData hmd = new HashMapData(new Carla(2, 0, 1),
					new ConnectionFactory().getTestConnection(), 1);
			relationships = hmd.searchBy("Nasser Othman Rahman",
					new RelationshipOccurrenceNumber());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}

		assertEquals(relationships.get(0).getPerson2().getName(),
				"Líbero Rodrigues");
		assertEquals(relationships.get(0).getScore(), 1.0d, delta);
		assertEquals(relationships.get(1).getPerson2().getName(),
				"Leandro Lunkes Amaral");
		assertEquals(relationships.get(1).getScore(), 0.625d, delta);
	}

}
