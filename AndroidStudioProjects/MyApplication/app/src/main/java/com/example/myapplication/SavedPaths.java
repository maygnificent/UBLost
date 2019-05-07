package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

public class SavedPaths extends AppCompatActivity {

    // variables
    private Button btnChoose, btnUpload;
    ImageView imageView;
    Button Gallery;
    Uri filePath;
    final int PICK_IMAGE_REQUEST = 71;
    private ActionBarDrawerToggle menuToggle;
    BottomNavigationView bottomNavigationView;

    // createmap
    private ArFragment arFragment;
    private ArrayList<Anchor> anchorList = new ArrayList<Anchor>();
    private boolean userIsDone = false;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_paths);
        imageView = findViewById(R.id.imgView);
        // Firebase Init
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        bottomNavigationView = findViewById(R.id.savedpaths_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.choose:
                        chooseImage();
                        break;
                    case R.id.upload:
                        uploadImage();
                        break;
                    case R.id.gallery_paths:
                        openGallery();
                        break;
                }
                return true;
            }
        });
        // ar session stuff
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();
            anchorList.add(anchor);

            // save anchor to firebase?
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(chooseCorrectModel()))
                    .build()
                    .thenAccept(modelRenderable -> addModeltoScene(anchor, modelRenderable))
                    .exceptionally(throwable -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(throwable.getMessage())
                                .show();
                        return null;
                    });
        });
        // undo button
        Button undoButton = findViewById(R.id.undo_button);
        undoButton.setOnClickListener(
                (unusedView) -> {
                    // undo:
                    // - remove previously saved anchor from anchorList
                    // - remove last anchor + its rendering
                    if (anchorList.isEmpty()) {
                        Context context = getApplicationContext();
                        CharSequence text = "You haven't placed down any arrows!";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    } else {
                        int lastAnchorIndex = anchorList.size() - 1;
                        Anchor lastAnchor = anchorList.remove(lastAnchorIndex);
                        deleteAnchor(lastAnchor);
                    }
                });
        // clear button
        Button clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(
                (unusedView) -> {
                    // clear:
                    // - make anchorList empty
                    // - remove all anchors and their rendering
                    if (anchorList.isEmpty()) {
                        Context context = getApplicationContext();
                        CharSequence text = "You haven't placed down any arrows!";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    } else {
                    for (int i = 0; i < anchorList.size(); i++) {
                        deleteAnchor(anchorList.get(i)); }
                    anchorList.clear();
                    }
                });
        // done button
        Button doneButton = findViewById(R.id.done_button);
        doneButton.setOnClickListener(
                (unusedView) -> {
                    // done:
                    // - set userisDone to true
                    // - call saveAnchorsToCloud to save those anchors to the cloud
                    // - create ShareDialogFragment (prompt asking for user email to share with)
                    userIsDone = true;
                    this.saveAnchorsToCloud(anchorList);
                    ShareDialogFragment dialog = new ShareDialogFragment();
                });
    }

    private void deleteAnchor(Anchor delanchor) {
        delanchor.detach();
    }

    private String chooseCorrectModel() {
        if (anchorList.size() == 1 || userIsDone) {
            return "model_start_end.sfb";}
        else {
            return "model.sfb";
        }
    }

    // TODO: saveAnchorsToCloud method: firebase and easter egg and all that stuff
    private void saveAnchorsToCloud(ArrayList<Anchor> savetheseanchors) {

    }

    private void addModeltoScene(Anchor anchor, ModelRenderable modelRenderable)  {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode); //always add parent after addchild;
        // otherwise, scene will not bring up the child of the parent node aka the transformable node
        transformableNode.select();

    }

    private void uploadImage() {
        if(filePath != null){
            final ProgressDialog progressDialog = new ProgressDialog((this));
            progressDialog.setTitle("Saving...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/*" + UUID.randomUUID().toString());
            ref.putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    progressDialog.dismiss();
                    Toast.makeText(SavedPaths.this, "Saved", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(SavedPaths.this, "Failed to Save" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Saving your path... " + (int)progress + "%");
                }
            });
        }

    }

    public void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Path"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!= null && data.getData() != null){
            filePath = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void openGallery(){

    }


}
