package przedlando;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/api")
public class RestEndpoint {

    @GET
    @Path("ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello!";
    }

    @GET
    @Path("recommendations/{user_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> recommendations(@PathParam("user_id") String userId) throws IOException, TasteException {
        DataModel datamodel = new FileDataModel(new File("C:/workspace_f/Rest/src/main/resources/dataset.csv"));
        UserSimilarity usersimilarity = new PearsonCorrelationSimilarity(datamodel);
        UserNeighborhood userneighborhood = new ThresholdUserNeighborhood(0.1, usersimilarity, datamodel);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(datamodel, userneighborhood, usersimilarity);
        List<RecommendedItem> recommendations = recommender.recommend(Long.valueOf(userId), 5);
        List<String> ids = new ArrayList<>();
        for (RecommendedItem recommendation : recommendations) {
            ids.add(Long.toString(recommendation.getItemID()));
        }
        return ids;
    }

}
