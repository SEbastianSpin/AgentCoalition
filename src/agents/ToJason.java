package agents;

import java.util.Queue;

import com.ai.astar.Node;
import com.google.gson.Gson;

public class ToJason {
    public static String toJson(Node[][] astarArray, Queue<PackageTask> packageTaskQueue) {

        try {
            Gson gson = new Gson();
            DataWrapper dataWrapper = new DataWrapper(astarArray, packageTaskQueue);

            // Convert the wrapper object to JSON
            return gson.toJson(dataWrapper);
        } catch(Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

}




