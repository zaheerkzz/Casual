package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.Relationship;
import model.rank.Judge;
import control.Carla;
import control.HashMapData;

@WebServlet(name = "search", urlPatterns = { "/search" })
public class SearchServlet extends HttpServlet {

	private static HashMapData indexedData;
	private Carla carla;
	private int itensPerPage = 10;
	private double minScore = 0.5;
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			Configuration.getInstance();
			this.carla = new Carla(2, 0.1, 0.9);
			indexedData = new HashMapData(this.carla, Configuration.getInstance().getPopularityDispersion());
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String parametro = request.getParameter("search");
		System.out.println(parametro);

		int pag = 1;
		String pagina = request.getParameter("pag");
		if (pagina != null) {
			if (!pagina.isEmpty()) {
				pag = Integer.parseInt(pagina);
			}
		}

		Class<? extends Judge> searchComputer = Configuration.getInstance()
				.getSearchComputer();

		List<Relationship> relationships = null;

		Judge rr = null;
		try {
			rr = searchComputer.newInstance();
			relationships = indexedData.searchBy(parametro, rr);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ServletException(e);
		}

		String didYouMean = null;
		int total = 1;
		boolean notFound = false;
		// se não achar ninguém ou tiver poucos resultados
		if (relationships == null || relationships.size() < itensPerPage) {
			didYouMean = indexedData.didYouMean(parametro, this.minScore);
		}

		if (didYouMean == null && relationships == null) {
			notFound = true;
		}

		if (relationships != null) {
			total = (int) Math.ceil((double) relationships.size()
					/ this.itensPerPage);
			System.out.println("Número de páginas: " + total);

			relationships = this.pagination(relationships, pag);

			this.print(relationships);
		}

		request.setAttribute("notfound", notFound);
		request.setAttribute("didyoumean", didYouMean);
		request.setAttribute("relationships", relationships);
		request.setAttribute("pag", pag);
		request.setAttribute("total", total);

		RequestDispatcher rd = request.getRequestDispatcher("/search.jsp");
		rd.forward(request, response);
	}

	private void print(List<Relationship> relationships) {
		System.out.println("Name person 1: "
				+ relationships.get(0).getPerson1().getName());
		for (Relationship relationship : relationships) {
			System.out.println("Name person 2: "
					+ relationship.getPerson2().getName() + ", score: "
					+ relationship.getScore() + ", popularity: "
					+ relationship.getPerson2().getPopularity());
		}
	}

	public List<Relationship> pagination(List<Relationship> list, int position) {
		ArrayList<Relationship> sublist = new ArrayList<>();
		int maxRange = position * itensPerPage;
		int minRange = (position - 1) * itensPerPage;
		int length;
		int numOfRelationships = list.size();
		if (numOfRelationships <= maxRange) {
			length = numOfRelationships;
		} else {
			length = maxRange;
		}
		for (int i = minRange; i < length; i++) {
			sublist.add(list.get(i));
		}
		return sublist;
	}
	
	public static HashMapData getIndexedData() {
		return indexedData;
	}
	
}
