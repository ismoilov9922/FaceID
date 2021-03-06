package uz.faceid.faceidcompany.libs.benchmark;


import static uz.faceid.faceidcompany.common.BitmapOperations.loadBitmapFromUri;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import uz.faceid.faceidcompany.R;
import uz.faceid.faceidcompany.activities.CameraPreviewActivity;
import uz.faceid.faceidcompany.common.ToastWrapper;

/**
 * Specifies all operations within Add photo layout. Provides events and process given data.
 */
public class AddPhotoLayout implements LayoutClassInterface {
    private final ActivityResultLauncher<Intent> choosePhotoLauncher;
    private final ActivityResultLauncher<Intent> takePhotoLauncher;
    private final ToastWrapper toastWrapper;
    public ArrayList<Bitmap> testPhotos = new ArrayList<>();
    private AppCompatActivity caller;
    private LayoutClassInterface benchmarkLayout;
    private ArrayList<Bitmap> tempPhotos = new ArrayList<>();
    private ImageView addPhoto_ImageView;
    private Button leftButton;
    private Button rightButton;
    private Button deletePhotoButton;
    private TextView numberOfPhotosTextView;
    private int indexer = 0;

    public AddPhotoLayout(AppCompatActivity caller, LayoutClassInterface benchmarkLayout) {
        this.caller = caller;
        this.benchmarkLayout = benchmarkLayout;
        toastWrapper = new ToastWrapper(caller);
        choosePhotoLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Process picked image
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Single photo case
                        if (result.getData().getData() != null) {
                            Bitmap photo = loadBitmapFromUri(result.getData().getData(), caller);
                            if (photo != null)
                                addNewTempPhoto(photo);
                        }
                        // Many photo case
                        else if (result.getData().getClipData() != null) {
                            ClipData clipData = result.getData().getClipData();
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Bitmap photo = loadBitmapFromUri(clipData.getItemAt(i).getUri(), caller);
                                if (photo != null)
                                    addNewTempPhoto(photo);
                            }
                        }
                        // No photo case
                        else {
                            toastWrapper.showToast(caller.getString(R.string.BenchmarkMode_NoPhotoSelected_Toast), Toast.LENGTH_LONG);
                        }
                    }
                });

        takePhotoLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Process photo taken from camera
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Get filename from activity result, read photo from internal app storage and process it
                        try {
                            String filename = result.getData().getStringExtra(CameraPreviewActivity.CAPTURED_FRAME_KEY);
                            FileInputStream fis = caller.getApplicationContext().openFileInput(filename);
                            Bitmap photo = BitmapFactory.decodeStream(fis);
                            addNewTempPhoto(photo);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void makeActive() {
        caller.setContentView(R.layout.activity_benchmark_add_photo);
        Button cancel = caller.findViewById(R.id.benchCancelButton);
        cancel.setOnClickListener(v -> cancel());

        Button add = caller.findViewById(R.id.benchAddButton);
        add.setOnClickListener(v -> addTestPhotos());

        Button chosePhoto = caller.findViewById(R.id.benchChoosePhotoButton);
        chosePhoto.setOnClickListener(v -> chosePhoto());

        Button makePhoto = caller.findViewById(R.id.benchMakePhotoButton);
        makePhoto.setOnClickListener(v -> makePhoto());

        leftButton = caller.findViewById(R.id.leftButton);
        leftButton.setOnClickListener(v -> previousPhoto());
        rightButton = caller.findViewById(R.id.rightButton);
        rightButton.setOnClickListener(v -> nextPhoto());
        addPhoto_ImageView = caller.findViewById(R.id.benchSelectedImage);

        deletePhotoButton = caller.findViewById(R.id.deletePhotoButton);
        deletePhotoButton.setOnClickListener(v -> deletePhoto());

        numberOfPhotosTextView = caller.findViewById(R.id.numberOfPhotosTextView);
        updateUI();
    }

    /**
     * Cancel being in this layout, return to previous layout. Clear all uploaded photos.
     */
    private void cancel() {
        tempPhotos.clear();
        benchmarkLayout.makeActive();
    }

    /**
     * Add all given photo to be proceed. Return to main benchmark layout.
     */
    private void addTestPhotos() {
        testPhotos.addAll(tempPhotos);
        tempPhotos.clear();
        benchmarkLayout.makeActive();
    }

    /**
     * Call camera activity and proceed it after return.
     */
    private void makePhoto() {
        Intent takePhotoIntent = new Intent(caller, CameraPreviewActivity.class);
        takePhotoIntent.putExtra(CameraPreviewActivity.CAMERA_MODE_KEY,
                CameraPreviewActivity.CameraPreviewMode.CAPTURE);
        takePhotoLauncher.launch(takePhotoIntent);
    }

    /**
     * Call intent to chose photo from disk and proceed it after return.
     */
    private void chosePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        choosePhotoLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    /**
     * Refresh UI and add photo to proper structures.
     *
     * @param image image added by user
     */
    private void addNewTempPhoto(Bitmap image) {
        tempPhotos.add(image);
        indexer = tempPhotos.size() - 1;
        updateUI();
    }

    /**
     * Update UI after some user operation. It will change display photo if required and menage
     * other UI part such as button visibility and correctness of texts.
     */
    private void updateUI() {
        if (tempPhotos.size() > 0) {
            addPhoto_ImageView.setImageBitmap(tempPhotos.get(indexer));
            deletePhotoButton.setVisibility(View.VISIBLE);
        } else {
            addPhoto_ImageView.setImageResource(0);
            deletePhotoButton.setVisibility(View.INVISIBLE);
        }

        if (indexer == 0) {
            leftButton.setVisibility(View.INVISIBLE);
        } else {
            leftButton.setVisibility(View.VISIBLE);
        }
        if (indexer >= tempPhotos.size() - 1) {
            rightButton.setVisibility(View.INVISIBLE);
        } else {
            rightButton.setVisibility(View.VISIBLE);
        }

        numberOfPhotosTextView.setText("Liczba zdj????: " + tempPhotos.size());
    }

    /**
     * Set next photo to be displayed.
     */
    private void nextPhoto() {
        indexer++;
        updateUI();
    }

    /**
     * Set previous photo to be displayed.
     */
    private void previousPhoto() {
        indexer--;
        updateUI();
    }

    /**
     * Delete photo from set to be added to tests.
     */
    private void deletePhoto() {
        tempPhotos.remove(indexer);
        if (indexer > 0)
            indexer--;
        updateUI();
    }
}
