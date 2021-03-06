package uz.faceid.faceidcompany.libs.facerecognition;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class NeuralModelProvider {
    private final static Map<String, NeuralModel> neuralModelsStorage = new HashMap<>();

    /**
     * Singleton NeuralModel instance getter. Initializes NeuralModel per name instance if not initialized earlier.
     * Returns static NeuralModel instance.
     *
     * @param context       - app/activity context
     * @param modelTag      - tag of the neural network model
     * @return instance - singleton FaceNetModel instance
     */
    public static NeuralModel getInstance(Context context, String modelTag) {
        NeuralModel neuralModel = neuralModelsStorage.get(modelTag);

        if (neuralModel == null) {
            synchronized (NeuralModel.class) {
                if (neuralModel == null) {
                    neuralModel = new NeuralModel(modelTag, modelTag + ".tflite", context);
                    neuralModelsStorage.put(modelTag,neuralModel);
                }
            }
        }

        return neuralModel;
    }
}
