package przedlando;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;

@Path("/api")
public class RestEndpoint {

    private File dataMatrix;

    @PostConstruct
    private void init() {
        dataMatrix = new File(RestEndpoint.class.getResource("../dataset.csv").getPath());
    }

    @GET
    @Path("ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello!";
    }

    @GET
    @Path("recommendations/{user_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getRecommendations(@PathParam("user_id") String userId) throws IOException, TasteException {
        DataModel datamodel = new FileDataModel(dataMatrix);

        String hostName = "przedlando.database.windows.net";
        String dbName = "przedlando";
        String user = "przedlando";
        String password = "LamaLamaDuck#";
        String url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;", hostName, dbName, user, password);
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url);
            String schema = connection.getSchema();



            // Create and execute a SELECT SQL statement.
            String selectSQL = "SELECT * FROM taste_preferences";



            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(selectSQL)) {


                while (resultSet.next())
                {
                    System.out.println(resultSet.getString(1) + " "
                            + resultSet.getString(2));
                }
                connection.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //DataModel dataModel2 = new GenericDataModel();
        UserSimilarity usersimilarity = new TanimotoCoefficientSimilarity(datamodel);
        UserNeighborhood userneighborhood = new ThresholdUserNeighborhood(0.6, usersimilarity, datamodel);
        UserBasedRecommender recommender = new GenericBooleanPrefUserBasedRecommender(datamodel, userneighborhood, usersimilarity);
        List<RecommendedItem> recommendations = recommender.recommend(Long.valueOf(userId), 5);
        List<String> ids = new ArrayList<>();
        for (RecommendedItem recommendation : recommendations) {
            ids.add(Long.toString(recommendation.getItemID()));
        }
        return ids;
    }

    @POST
    @Path("recommendations/{user_id}/{product_id}")
    public Response addRecommendation(@PathParam("user_id") String userId, @PathParam("product_id") String productId) {

        String hostName = "przedlando.database.windows.net";
        String dbName = "przedlando";
        String user = "przedlando";
        String password = "LamaLamaDuck#";
        String url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;", hostName, dbName, user, password);
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url);
            String schema = connection.getSchema();



            // Create and execute a SELECT SQL statement.
            String insertSQL = "INSERT INTO taste_preferences (user_id, item_id, preference) VALUES ("
                    + userId + ", " + productId + ", 5);";

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(insertSQL)) {



                connection.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dataMatrix, true));
            bufferedWriter.write(userId + "," + productId);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }

}
