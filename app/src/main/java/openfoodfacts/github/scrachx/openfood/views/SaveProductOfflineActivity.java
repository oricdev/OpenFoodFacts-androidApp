package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class SaveProductOfflineActivity extends BaseActivity {

    @Bind(R.id.imageSave) ImageView imgSave;
    @Bind(R.id.editTextName) EditText name;
    @Bind(R.id.editTextStores) EditText store;
    @Bind(R.id.editTextWeight) EditText weight;
    @Bind(R.id.spinnerImages) Spinner spinnerI;
    @Bind(R.id.spinnerUnitWeight) Spinner spinnerW;
    @Bind(R.id.buttonTakePicture) Button takePic;
    @Bind(R.id.buttonFromGallery) Button takeGallery;
    @Bind(R.id.buttonSaveProduct) Button save;

    private SendProduct mProduct = new SendProduct();
    private String mBarcode = null;
    private final String[] mUnit = new String[1];
    private final String[] mImage = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_product_offline);

        EasyImage.configuration(this)
                .setImagesFolderName("OFF_Images")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);

        final SharedPreferences settings = getSharedPreferences("temp", 0);
        mBarcode = settings.getString("barcode", "");

        imgSave.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapterW = ArrayAdapter.createFromResource(this, R.array.units_array, R.layout.custom_spinner_item);
        adapterW.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerW.setAdapter(adapterW);

        ArrayAdapter<CharSequence> adapterI = ArrayAdapter.createFromResource(this, R.array.images_array, R.layout.custom_spinner_item);
        adapterI.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerI.setAdapter(adapterI);

        List<SendProduct> sp = SendProduct.find(SendProduct.class, "barcode = ?", mBarcode);
        if (sp.size() > 0) {
            SendProduct product = sp.get(0);
            mProduct = product;
        }
        if(mProduct != null) {
            if(!mProduct.getImgupload_front().isEmpty()) {
                Picasso.with(this)
                        .load(mProduct.getImgupload_front())
                        .fit()
                        .centerCrop()
                        .into(imgSave);
            }
            name.setText(mProduct.getName());
            store.setText(mProduct.getStores());
            weight.setText(mProduct.getWeight());
            ArrayAdapter unitAdapter = (ArrayAdapter) spinnerW.getAdapter();
            int spinnerPosition = unitAdapter.getPosition(mProduct.getWeight_unit());
            spinnerW.setSelection(spinnerPosition);
            spinnerI.setSelection(0);
        }
    }
    @OnItemSelected(value = R.id.spinnerUnitWeight, callback = OnItemSelected.Callback.ITEM_SELECTED)
    protected void onUnitSelected(int pos) {
        mUnit[0] = spinnerW.getItemAtPosition(pos).toString();
    }

    @OnItemSelected(value = R.id.spinnerImages, callback = OnItemSelected.Callback.ITEM_SELECTED)
    protected void onImageSelected(int pos) {
        mImage[0] = spinnerI.getItemAtPosition(pos).toString();

        if(pos == 0) {
            if(!mProduct.getImgupload_front().isEmpty()) {
                imgSave.setVisibility(View.VISIBLE);
                Picasso.with(this)
                        .load(new File(mProduct.getImgupload_front()))
                        .fit()
                        .centerCrop()
                        .into(imgSave);
            } else {
                imgSave.setVisibility(View.GONE);
            }
        } else if(pos == 1) {
            if(!mProduct.getImgupload_nutrition().isEmpty()) {
                imgSave.setVisibility(View.VISIBLE);
                Picasso.with(this)
                        .load(new File(mProduct.getImgupload_nutrition()))
                        .fit()
                        .centerCrop()
                        .into(imgSave);
            } else {
                imgSave.setVisibility(View.GONE);
            }
        } else {
            if(!mProduct.getImgupload_ingredients().isEmpty()) {
                imgSave.setVisibility(View.VISIBLE);
                Picasso.with(this)
                        .load(new File(mProduct.getImgupload_ingredients()))
                        .fit()
                        .centerCrop()
                        .into(imgSave);
            } else {
                imgSave.setVisibility(View.GONE);
            }
        }

    }

    @OnClick(R.id.buttonFromGallery)
    protected void onChooserWithGalleryClicked() {
        EasyImage.openChooserWithGallery(this, "Images", 0);
    }

    @OnClick(R.id.buttonSaveProduct)
    protected void onSaveProduct() {
        if (!mProduct.getImgupload_front().isEmpty() && !name.getText().toString().isEmpty()) {
            if (mProduct != null) {
                mProduct.setBarcode(mBarcode);
                mProduct.setName(name.getText().toString());
                mProduct.setImgupload_front(mProduct.getImgupload_front());
                mProduct.setImgupload_ingredients(mProduct.getImgupload_ingredients());
                mProduct.setImgupload_nutrition(mProduct.getImgupload_nutrition());
                mProduct.setStores(store.getText().toString());
                mProduct.setWeight(weight.getText().toString());
                mProduct.setWeight_unit(mUnit[0]);
                mProduct.save();
            }
            Toast.makeText(getApplicationContext(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            SaveProductOfflineActivity.this.finish();
        } else {
            Toast.makeText(getApplicationContext(), R.string.txtPictureNeeded, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.buttonTakePicture)
    protected void onTakePhotoClicked() {
        EasyImage.openCamera(this, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                onPhotoReturned(imageFile);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(SaveProductOfflineActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });

    }

    private void onPhotoReturned(File photoFile) {
        if(spinnerI.getSelectedItemPosition() == 0) {
            mProduct.setImgupload_front(photoFile.getAbsolutePath());
            imgSave.setVisibility(View.VISIBLE);
        } else if(spinnerI.getSelectedItemPosition() == 1) {
            mProduct.setImgupload_nutrition(photoFile.getAbsolutePath());
            imgSave.setVisibility(View.VISIBLE);
        } else {
            mProduct.setImgupload_ingredients(photoFile.getAbsolutePath());
            imgSave.setVisibility(View.VISIBLE);
        }
        Picasso.with(this)
                .load(photoFile)
                .fit()
                .centerCrop()
                .into(imgSave);
    }
}
