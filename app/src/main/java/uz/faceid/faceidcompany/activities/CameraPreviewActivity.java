package uz.faceid.faceidcompany.activities;

import static java.lang.Math.max;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.view.PreviewView;

import com.google.mlkit.vision.face.Face;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uz.faceid.faceidcompany.R;
import uz.faceid.faceidcompany.common.PermissionsWrapper;
import uz.faceid.faceidcompany.common.TransitionsLibrary;
import uz.faceid.faceidcompany.customviews.BoundingBox;
import uz.faceid.faceidcompany.libs.customcamera.CameraPreview;
import uz.faceid.faceidcompany.libs.customcamera.CameraPreviewListener;
import uz.faceid.faceidcompany.libs.facerecognition.FaceDetector;
import uz.faceid.faceidcompany.libs.facerecognition.FaceRecognizer;
import uz.faceid.faceidcompany.libs.globaldata.GlobalData;
import uz.faceid.faceidcompany.libs.globaldata.ModelObject;

public class CameraPreviewActivity extends AppCompatActivity implements CameraPreviewListener {
    private static final String Tag = "CameraPreviewActivity";
    public static final String CAMERA_MODE_KEY = "CameraPreviewMode";
    public static final String CAPTURED_FRAME_KEY = "CapturedFrame";
    private final List<BoundingBox> boundingBoxesList = new ArrayList<>();
    private RelativeLayout constraintLayout;
    private ImageButton switchCameraButton;
    private ImageButton cameraCaptureButton;
    private PreviewView previewView;
    private FaceDetector faceDetector;
    private FaceRecognizer faceRecognizer;
    private CameraPreview cameraPreview;
    private CameraPreviewMode cameraPreviewMode;
    private boolean captureNextFrame = false;
    private boolean frameCaptured = false;

    /**
     * Called on activity create event.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera_preview);

        // Get cameraPreviewMode from intent
        cameraPreviewMode = (CameraPreviewMode) getIntent().
                getSerializableExtra(CAMERA_MODE_KEY);
        if (cameraPreviewMode == null) {
            cameraPreviewMode = CameraPreviewMode.RECOGNITION;
        }

        // Prepare switchCamera button
        switchCameraButton = findViewById(R.id.switchCameraButton);
        assert switchCameraButton != null;

        switchCameraButton.setOnClickListener(v -> {
            hideAllBoxes();
            cameraPreview.switchCamera();
        });

        // Prepare cameraCapture button
        if (cameraPreviewMode == CameraPreviewMode.CAPTURE) {
            cameraCaptureButton = findViewById(R.id.cameraCaptureButton);
            assert cameraCaptureButton != null;

            cameraCaptureButton.setVisibility(View.VISIBLE);
            cameraCaptureButton.setOnClickListener(v -> {
                captureNextFrame = true;
            });
        }

        // Get constraintLayout
        constraintLayout = findViewById(R.id.cameraPreviewLayout);
        assert constraintLayout != null;

        // Get previewView
        previewView = findViewById(R.id.cameraPreviewView);
        assert previewView != null;

        // Validate app permissions
        final List<String> targetPermissions = Arrays.asList(
                Manifest.permission.CAMERA
        );
        PermissionsWrapper.validatePermissions(targetPermissions, this);

        // Get model object instance
        Context context = getApplicationContext();
        SharedPreferences userSettings = GlobalData.getUserSettings(context);
        ModelObject modelObject = GlobalData.getModel(getApplicationContext(),
                userSettings.getString(
                        getString(R.string.settings_userModel_key),
                        getResources().getStringArray(R.array.models)[0]),
                userSettings.getString(
                        getString(R.string.settings_userModel_key),
                        getResources().getStringArray(R.array.models)[0]));

        faceDetector = new FaceDetector();
        if (cameraPreviewMode == CameraPreviewMode.RECOGNITION) {
            faceRecognizer = new FaceRecognizer(modelObject.neuralModel, modelObject.userDatabase);
        }

        // Initialize camera preview
        cameraPreview = new CameraPreview(this, previewView);
        cameraPreview.setListener(this);
        cameraPreview.openCamera();
    }

    /**
     * Called on pause event.
     */
    @Override
    protected void onPause() {
        super.onPause();
        hideAllBoxes();
        cameraPreview.removeListener(this);
        cameraPreview.closeCamera();
    }

    /**
     * Called on resume event.
     */
    @Override
    protected void onResume() {
        super.onResume();
        cameraPreview.setListener(this);
        cameraPreview.openCamera();
    }

    /**
     * Hide all bounding Boxes in activity.
     */
    private void hideAllBoxes() {
        for (int i = 0; i < boundingBoxesList.size(); i++) {
            boundingBoxesList.get(i).setVisibility(false);
        }
    }

    /**
     * Called on new camera frame event.
     *
     * @param newFrame        captured frame
     * @param rotationDegrees rotation of the captured frame
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onCameraFrame(Bitmap newFrame, int rotationDegrees) {
        // Transform new frame to fit the face detection input
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        Bitmap transformedFrame = Bitmap.createBitmap(newFrame, 0, 0, newFrame.getWidth(), newFrame.getHeight(), matrix, true);

        // Capture the frame if required
        if (captureNextFrame) {
            if (!frameCaptured) {
                runOnUiThread(() -> {
                    returnFrame(transformedFrame);
                });
                frameCaptured = true;
            }
            return;
        }

        // Start face detection and recognition and collect the results if possible
        List<Face> detectedFaces = faceDetector.detectFaces(transformedFrame);

        String[] recognizedNames = new String[0];
        if (cameraPreviewMode == CameraPreviewMode.RECOGNITION) {
            recognizedNames = faceRecognizer.recogniseFaces(transformedFrame, detectedFaces);
        }

        if (detectedFaces != null) {
            // Prepare proper number of BoundingBoxes for this frame
            runOnUiThread(() -> {
                prepareBoundingBoxes(detectedFaces.size());


                // Hide unused boxes on default
                for (int i = detectedFaces.size(); i < boundingBoxesList.size(); i++) {
                    boundingBoxesList.get(i).setVisibility(false);
                }
            });

            for (int i = 0; i < detectedFaces.size(); i++) {
                // Map face coordinates to screen coordinates
                Size frameDimensions = new Size(transformedFrame.getWidth(), transformedFrame.getHeight());
                final RectF mappedFaceLocation = mapOutputCoordinates(detectedFaces.get(i).getBoundingBox(), frameDimensions);

                Log.d(Tag, String.format("Found face at [%f, %f, %f, %f]",
                        mappedFaceLocation.left,
                        mappedFaceLocation.top,
                        mappedFaceLocation.right,
                        mappedFaceLocation.bottom));

                // Only Ui Thread can touch view hierarchy
                int currIndex = i;
                String[] finalRecognizedNames = recognizedNames;
                runOnUiThread(() -> {
                    boundingBoxesList.get(currIndex).setPosition(
                            Math.max((int) mappedFaceLocation.left, 0),
                            Math.max((int) mappedFaceLocation.top, 0),
                            (int) mappedFaceLocation.right,
                            (int) mappedFaceLocation.bottom);
                    // Draw label for detected face
                    if (cameraPreviewMode == CameraPreviewMode.RECOGNITION) {
                        if (finalRecognizedNames != null && finalRecognizedNames.length > currIndex) {
                            boundingBoxesList.get(currIndex).setBoxCaption(finalRecognizedNames[currIndex]);
                        }
                    }
                    boundingBoxesList.get(currIndex).setVisibility(true);
                });
            }
        }
    }

    /**
     * Save given frame to file in internal app storage, insert filename in
     * activity result and finish the activity.
     *
     * @param frame to be saved
     */
    private void returnFrame(Bitmap frame) {
        if (frame == null) {
            throw new NullPointerException("Unexpected null frame");
        }

        try {
            String filename = "capturedFrame";
            FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            frame.compress(Bitmap.CompressFormat.PNG, 100, fos);

            Intent resultData = new Intent();
            resultData.putExtra(CAPTURED_FRAME_KEY, filename);
            setResult(RESULT_OK, resultData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Close activity
        cameraPreview.closeCamera();
        finish();
        TransitionsLibrary.executeToRightTransition(this);
    }

    private void prepareBoundingBoxes(int targetBoxesNum) {
        final int minBoxesNum = 10;
        targetBoxesNum = max(minBoxesNum, targetBoxesNum);
        int currBoxesNum = boundingBoxesList.size();

        if (targetBoxesNum >= currBoxesNum) {
            // Add missing boxes
            for (int i = 0; i < targetBoxesNum - currBoxesNum; i++) {
                BoundingBox newBox = new BoundingBox(this, null);
                boundingBoxesList.add(newBox);
                constraintLayout.addView(newBox);
                newBox.setVisibility(false);
            }
        } else {
            // Remove unnecessary boxes
            for (int i = 0; i < currBoxesNum - targetBoxesNum; i++) {
                constraintLayout.removeView(boundingBoxesList.get(boundingBoxesList.size() - 1));
                boundingBoxesList.remove(boundingBoxesList.size() - 1);
            }
        }
    }

    /**
     * Map face's coordinates from frame space to screen space.
     *
     * @param faceLocation    face coordinates in frame space
     * @param frameDimensions dimensions of frame
     * @return face coordinates in screen space
     */
    private RectF mapOutputCoordinates(Rect faceLocation, Size frameDimensions) {

        // Transform face size to preview's
        RectF correctedLocation = new RectF(
                (float) faceLocation.left / frameDimensions.getWidth() * previewView.getWidth(),
                (float) faceLocation.top / frameDimensions.getHeight() * previewView.getHeight(),
                (float) faceLocation.right / frameDimensions.getWidth() * previewView.getWidth(),
                (float) faceLocation.bottom / frameDimensions.getHeight() * previewView.getHeight()
        );

        // Transform front camera mirroring
        if (cameraPreview.getLensFacing() == CameraSelector.LENS_FACING_FRONT) {
            correctedLocation = new RectF(
                    previewView.getWidth() - correctedLocation.right,
                    correctedLocation.top,
                    previewView.getWidth() - correctedLocation.left,
                    correctedLocation.bottom);
        }

        // Set location to preview's with given margin and aspectRatio
        float midX = (correctedLocation.left + correctedLocation.right) / 2f;
        float midY = (correctedLocation.top + correctedLocation.bottom) / 2f;

        RectF result = new RectF(
                midX - correctedLocation.width() / 2f,
                midY - correctedLocation.height() / 2f,
                midX + correctedLocation.width() / 2f,
                midY + correctedLocation.height() / 2f
        );

        // Keep these logs in case something still doesn't work
        if (false) {
            Log.d(Tag, String.format("Org size: %d x %d",
                    frameDimensions.getHeight(),
                    frameDimensions.getWidth()));
            Log.d(Tag, String.format("Org box ltrb: [%d, %d, %d, %d]",
                    faceLocation.left,
                    faceLocation.top,
                    faceLocation.right,
                    faceLocation.bottom));
            Log.d(Tag, String.format("Org box size: %d x %d",
                    faceLocation.bottom - faceLocation.top,
                    faceLocation.right - faceLocation.left));

            Log.d(Tag, String.format("Corrected box ltrb: [%d, %d, %d, %d]",
                    (int) correctedLocation.left,
                    (int) correctedLocation.top,
                    (int) correctedLocation.right,
                    (int) correctedLocation.bottom));
            Log.d(Tag, String.format("Corrected box size: %d x %d",
                    (int) correctedLocation.bottom - (int) correctedLocation.top,
                    (int) correctedLocation.right - (int) correctedLocation.left));

            Log.d(Tag, String.format("New size: %d x %d",
                    getResources().getDisplayMetrics().heightPixels,
                    getResources().getDisplayMetrics().widthPixels));
            Log.d(Tag, String.format("New box ltrb: [%d, %d, %d, %d]",
                    (int) result.left,
                    (int) result.top,
                    (int) result.right,
                    (int) result.bottom));
            Log.d(Tag, String.format("New box size: %d x %d",
                    (int) result.bottom - (int) result.top,
                    (int) result.right - (int) result.left));

            if (false) {
                Log.d(Tag, String.format("Relative width org: %f, new: %f",
                        (faceLocation.right - faceLocation.left) / (float) frameDimensions.getWidth(),
                        ((int) result.right - (int) result.left) / (float) getResources().getDisplayMetrics().widthPixels));
                Log.d(Tag, String.format("Relative height org: %f, new: %f",
                        (faceLocation.bottom - faceLocation.top) / (float) frameDimensions.getHeight(),
                        ((int) result.bottom - (int) result.top) / (float) getResources().getDisplayMetrics().heightPixels));
            }

            Log.d(Tag, String.format("Relative width diff: %f",
                    (faceLocation.right - faceLocation.left) / (float) frameDimensions.getWidth() -
                            ((int) result.right - (int) result.left) / (float) getResources().getDisplayMetrics().widthPixels));
            Log.d(Tag, String.format("Relative height diff: %f",
                    (faceLocation.bottom - faceLocation.top) / (float) frameDimensions.getHeight() -
                            ((int) result.bottom - (int) result.top) / (float) getResources().getDisplayMetrics().heightPixels));
        }

        return result;
    }

    public enum CameraPreviewMode {
        RECOGNITION,
        CAPTURE
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TransitionsLibrary.executeToRightTransition(this);
    }
}
