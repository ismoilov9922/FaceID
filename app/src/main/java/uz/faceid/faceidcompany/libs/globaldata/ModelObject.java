package uz.faceid.faceidcompany.libs.globaldata;

import android.content.Context;

import uz.faceid.faceidcompany.R;
import uz.faceid.faceidcompany.libs.facerecognition.NeuralModel;
import uz.faceid.faceidcompany.libs.facerecognition.NeuralModelProvider;
import uz.faceid.faceidcompany.libs.globaldata.userdatabase.Metric;
import uz.faceid.faceidcompany.libs.globaldata.userdatabase.UserDatabase;

public class ModelObject {
    public final NeuralModel neuralModel;
    public final UserDatabase userDatabase;

    /**
     * ModelObject constructor. Depending on the type, acquires proper neural network model.
     * Always creates new database with the unique filename assigned.
     *
     * @param context       - app/activity context
     * @param modelName     - name of the neural network model
     */
    ModelObject(Context context, String modelName, String databaseName) {
        neuralModel = NeuralModelProvider.getInstance(context, modelName);

        String[] models = context.getResources().getStringArray(uz.faceid.faceidcompany.R.array.models);
        String[] metrics = context.getResources().getStringArray(R.array.metrics);
        String[] thresholds = context.getResources().getStringArray(R.array.threshold);
        for(int i = 0; i < models.length; i++){
            if(models[i].equals(modelName)){
                Metric metric = Metric.valueOf(metrics[i].toUpperCase());
                float threshold = Float.parseFloat(thresholds[i]);
                userDatabase = new UserDatabase(context, databaseName, neuralModel.getOutputSize(),
                        true, metric, threshold);
                return;
            }
        }
        throw new AssertionError("Metrics or threshold not specified for " + modelName);
    }

    /**
     * Remove database of the ModelObject.
     */
    public void clear() {
        userDatabase.clear();
    }
}
