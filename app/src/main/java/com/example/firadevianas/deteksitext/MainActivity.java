package com.example.firadevianas.deteksitext;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    //camere text recog
    private ImageClassifier classifier;
    private Uri photoUri;
    public static final int REQUEST_IMAGE = 100;
    public static final int REQUEST_PERMISSION = 300;
    private String imageFilePath = "";
    private String datapath = "";
    private String lang = "";
    static TessBaseAPI sTess;
    private ProgressBar progressBar;
    String stringBrand,stringAmount,stringDate;
    Bitmap photo;
    String OCRresult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadFragment(new FragmentChart());
        progressBar = findViewById(R.id.progressBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        //recog image

        classifier = new ImageClassifier(MainActivity.this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }


        sTess = new TessBaseAPI();

        lang = "ind";
        datapath = getFilesDir()+ "/tesseract";

        if(checkFile(new File(datapath+"/tessdata")))
        {
            sTess.init(datapath, lang);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                openCameraIntent();
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        stringDate = format2.format(calendar.getTime());


    }



    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()){
            case R.id.nav_reportChart:
                fragment= new FragmentChart();
                break;
            case R.id.nav_addTransaction:
                fragment=new FragmentAddTransaction();
                break;
            case R.id.nav_account:
                fragment=new FragmentAccount();
                break;
        }
        return loadFragment(fragment);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE) {
                progressBar.setVisibility(View.GONE);
                Bitmap bitmap = getBitmapFromUri();
                Bitmap newBitmap = rotateImage(bitmap);
                photo = rotateImage(bitmap);
                new AsyncTess().execute(newBitmap);
            }
        }
    }

    private void showAlert() {
        if(MainActivity.this != null) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);

            builder.setTitle("Oh snap :(")
                    .setMessage("We can't get any amount information from the receipt, would you like to take another picture?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            openCameraIntent();
                        }
                    });
            final android.support.v7.app.AlertDialog alert = builder.create();
            alert.show();
        }
    }

    //image processing

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
            photoUri = FileProvider.getUriForFile(this, getPackageName() +".provider", photoFile);
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(pictureIntent, REQUEST_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thanks for granting Permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imageFilePath = image.getAbsolutePath();

        return image;
    }

    private Bitmap rotateImage(Bitmap bitmap){
        ExifInterface exifInterface = null;
        try{
            exifInterface = new ExifInterface(imageFilePath);
        }catch (IOException e){
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            default:
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        //imageView.setImageBitmap(rotatedBitmap);
        Classifier.Recognition r = classifier.classifyImage(rotatedBitmap);
        final List<Classifier.Recognition> results = new ArrayList<>();
        if (r.getConfidence() > 0.1) {
            results.add(r);
        }
        String result = results.toString();
        String resultBrand = result.replaceAll("[^a-zA-Z]","");
        stringBrand = resultBrand;
        //receipt.setText(resultBrand);

        return rotatedBitmap;
    }

    public static List<String> extractAmount(String text){
        List<String> containedText = new ArrayList<String>();
        String amountRegex = "((TOTAL\\s:\\s\\d+,\\d+)|(HARGA\\sJUAL\\s:\\s\\d,\\d+)|(TAL\\s:\\s\\d+,\\d+)|(Rp.\\d+.\\d+)|(Total\\s:\\sRp.\\s\\d+.\\d+)|(Total\\s\\d+.\\d+)|(Tota1:\\s\\d+,\\d+)|(Total:\\s\\d+,\\d+))";
        Pattern pattern = Pattern.compile(amountRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedText.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedText;
    }

    public Bitmap getBitmapFromUri() {

        getContentResolver().notifyChange(photoUri, null);
        ContentResolver cr = getContentResolver();
        Bitmap bitmap;

        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, photoUri);
            return bitmap;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    boolean checkFile(File dir){
        if(!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()){
            String datafilepath = datapath + "/tessdata/" + lang + ".traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
        return true;
    }
    void copyFiles(){
        AssetManager assetMgr = this.getAssets();

        InputStream is = null;
        OutputStream os = null;

        try {
            is = assetMgr.open("tessdata/"+lang+".traineddata");

            String destFile = datapath + "/tessdata/" + lang + ".traineddata";

            os = new FileOutputStream(destFile);

            byte[] buffer = new byte[2048];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            is.close();
            os.flush();
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private class AsyncTess extends AsyncTask<Bitmap,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            sTess.setImage(photo);
            return sTess.getUTF8Text();
        }
        protected void onPostExecute(String result){
            progressBar.setVisibility(View.GONE);
            OCRresult = result;
            List<String> resultTess = extractAmount(result);
            for(String hasilTess : resultTess){
                result =hasilTess;
            }
            stringAmount = result.replaceAll("[^0-9]", "");
            if(stringAmount.length()>9){
                showAlert();
                String getBrand = stringBrand;
                String getDate = stringDate;
                Bundle bundle = new Bundle();
                bundle.putString("getDate",getDate);
                bundle.putString("getBrand",getBrand);
                bundle.putString("getAmount","");
                Fragment fragmentAddTransaction = new FragmentAddTransaction();
                fragmentAddTransaction.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragmentAddTransaction);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }else if(stringBrand.equals("KOI")){
                char test1 = stringAmount.charAt(0);
                if(test1=='1'){
                    String getAmount = stringAmount.substring(1);
                    String getBrand = stringBrand;
                    String getDate = stringDate;
                    Bundle bundle = new Bundle();
                    bundle.putString("getDate",getDate);
                    bundle.putString("getBrand",getBrand);
                    bundle.putString("getAmount",getAmount);
                    FragmentAddTransaction fragmentAddTransaction = new FragmentAddTransaction();
                    fragmentAddTransaction.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragmentAddTransaction);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }else if(test1!='1'){
                    String getAmount = stringAmount;
                    String getBrand = stringBrand;
                    String getDate = stringDate;
                    Bundle bundle = new Bundle();
                    bundle.putString("getDate",getDate);
                    bundle.putString("getBrand",getBrand);
                    bundle.putString("getAmount",getAmount);
                    FragmentAddTransaction fragmentAddTransaction = new FragmentAddTransaction();
                    fragmentAddTransaction.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragmentAddTransaction);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }else if(stringBrand.equals("KFC")) {
                char test2 =stringAmount.charAt(2);
                if(test2=='1'){
                    StringBuilder getridOne = new StringBuilder(stringAmount);
                    getridOne.deleteCharAt(2);
                    String getRidChar = getridOne.toString();
                    String getAmount = getRidChar;
                    String getBrand = stringBrand;
                    String getDate = stringDate;
                    Bundle bundle = new Bundle();
                    bundle.putString("getDate",getDate);
                    bundle.putString("getBrand",getBrand);
                    bundle.putString("getAmount",getAmount);
                    FragmentAddTransaction fragmentAddTransaction = new FragmentAddTransaction();
                    fragmentAddTransaction.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragmentAddTransaction);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }else{
                    String getAmount = stringAmount;
                    String getBrand = stringBrand;
                    String getDate = stringDate;
                    Bundle bundle = new Bundle();
                    bundle.putString("getDate",getDate);
                    bundle.putString("getBrand",getBrand);
                    bundle.putString("getAmount",getAmount);
                    FragmentAddTransaction fragmentAddTransaction = new FragmentAddTransaction();
                    fragmentAddTransaction.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragmentAddTransaction);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            } else{
                String getAmount = stringAmount;
                String getBrand = stringBrand;
                String getDate = stringDate;
                Bundle bundle = new Bundle();
                bundle.putString("getDate",getDate);
                bundle.putString("getBrand",getBrand);
                bundle.putString("getAmount",getAmount);
                FragmentAddTransaction fragmentAddTransaction = new FragmentAddTransaction();
                fragmentAddTransaction.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragmentAddTransaction);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }



}


