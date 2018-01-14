package przedlando;

import org.apache.mahout.cf.taste.common.TasteException;
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
