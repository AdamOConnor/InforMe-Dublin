package com.example.adamoconnor.test02maps.PostingInformationAndComments;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.adamoconnor.test02maps.R;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddInformation extends AppCompatActivity {

    private static final String TAG = "Add Information";

    //declare edit texts
    private EditText monumentName;
    private EditText areaName;
    private EditText monumentInformation;

    //declare spinner.
    private Spinner location;

    // declare multiple image picker.
    private int PICK_IMAGE_MULTIPLE = 1;
    // declare image string.
    private String imageEncoded;
    //declare image list for multiple images
    private static List<String> imagesEncodedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmonument);

        //setting screen orientation to stop fragments view showing on eachother.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //setup back button action bar.
        setupActionBar();

        // declare gallery and send email buttons.
        Button gallery = (Button)findViewById(R.id.resetButton);
        Button sendEmail = (Button)findViewById(R.id.submitButton);

        //declare the textviews of the layout.
        monumentName = (EditText)findViewById(R.id.monumentName);
        areaName = (EditText)findViewById(R.id.area);
        monumentInformation = (EditText)findViewById(R.id.monumentInformation);
        location = (Spinner)findViewById(R.id.locationSpinner);


        // set the gallery OnClick listener.
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
            }
        });

        // set OnClick listener for sending of email.
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validateForm()) {
                    email("informedublinproject@gmail.com","",
                            "Add New Monument - InforMe@Dublin","Monument Name : "+monumentName.getText()+"\n"
                                    +"Location : "+location.getSelectedItem().toString()
                                    +"\n"+"Area name : "+areaName.getText()+"\n"+"Monument Information : "+monumentInformation.getText());
                }
            }
        });
    }

    /**
     * validating the text-fields on which the user
     * must enter their personal information.
     * @return
     * return the error on screen for the text-field.
     */
    private boolean validateForm() {
        boolean valid = true;

        String monument = monumentName.getText().toString();
        if (TextUtils.isEmpty(monument)) {
            monumentName.setError("Required.");
            valid = false;
        } else {
            monumentName.setError(null);
        }

        String area = areaName.getText().toString();
        if (TextUtils.isEmpty(area)) {
            areaName.setError("Required.");
            valid = false;
        } else {
            areaName.setError(null);
        }

        String description = monumentInformation.getText().toString();
        if (TextUtils.isEmpty(description)) {
            areaName.setError("Required.");
            valid = false;
        } else {
            areaName.setError(null);
        }

        return valid;
    }

    /**
     * set back button for action bar
     * @param item
     * find items on options menu
     * @return
     * return the item selected.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * used for choosing images for sending by email.
     * @param requestCode
     * picking multiple or single images.
     * @param resultCode
     * see if everything went ok.
     * @param data
     * get the data on which was chosen.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<>();
                if(data.getData()!=null){

                    Uri mImageUri=data.getData();

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);
                    cursor.close();

                }else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            // Get the cursor
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();
                        }

                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked an Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * setup the above action bar.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }

    /**
     * sending the following email to inforMe@Dublin.
     * @param emailTo
     * send email to.
     * @param emailCC
     * used for carbon copy of email.
     * @param subject
     * setting the subject of the email.
     * @param emailText
     * the text which will be in the email.
     */
    public void email(String emailTo, String emailCC,
                             String subject, String emailText)
    {
        //need to "send multiple" to get more than one attachment
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/xml");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[]{emailTo});
        emailIntent.putExtra(android.content.Intent.EXTRA_CC,
                new String[]{emailCC});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailText);
        //has to be an ArrayList
        ArrayList<Uri> uris = new ArrayList<>();
        //convert from paths to Android friendly Parcelable Uri's
        try {
            for (String file : imagesEncodedList)
            {
                File fileIn = new File(file);
                Uri u = Uri.fromFile(fileIn);
                uris.add(u);
            }
        } catch (NullPointerException ex) {
            ex.getStackTrace();
        }

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        this.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
}
