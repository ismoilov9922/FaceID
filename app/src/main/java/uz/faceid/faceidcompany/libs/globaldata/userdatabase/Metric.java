package uz.faceid.faceidcompany.libs.globaldata.userdatabase;


import java.util.function.BiFunction;
import java.util.function.Function;

import uz.faceid.faceidcompany.common.VectorOperations;

public enum Metric {
    EUCLIDEAN, EUCLIDEAN_NORM, COSINE;

    public static BiFunction<float[], float[], Double> getDistanceFunction(Metric metric){
        switch (metric){
            case COSINE:
                return VectorOperations::cosineSimilarity;
            default:
                return VectorOperations::euclideanDistance;
        }
    }

    public static Function<float[], float[]> getNormalizationFunction(Metric metric){
        switch (metric){
            case EUCLIDEAN_NORM:
                return VectorOperations::l2Normalize;
            default:
                return (x) -> x;
        }
    }
}