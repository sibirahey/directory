package org.jdc.template.ux.individualedit;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.dbtools.android.domain.date.DBToolsThreeTenFormatter;
import org.jdc.template.InternalIntents;
import org.jdc.template.R;
import org.jdc.template.inject.Injector;
import org.jdc.template.model.database.main.individual.Individual;
import org.jdc.template.ui.activity.BaseActivity;
import org.jdc.template.ui.fragment.DatePickerFragment;
import org.jdc.template.util.AppConstants;
import org.jdc.template.ux.individual.IndividualContract;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import pocketknife.PocketKnife;

public class IndividualEditActivity extends BaseActivity implements IndividualEditContract.View, AppBarLayout.OnOffsetChangedListener {

    private final String TAG = "IndividualEditActivity";

    private SharedPreferences sharedPreferences;

    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.mainToolbar)
    Toolbar toolbar;
    @BindView(R.id.firstNameLayout)
    TextInputLayout firstNameLayout;
    @BindView(R.id.firstNameEditText)
    EditText firstNameEditText;
    @BindView(R.id.lastNameEditText)
    EditText lastNameEditText;
    @BindView(R.id.birthDateEditText)
    EditText birthDateEditText;
    @BindView(R.id.affiliationEditText)
    EditText affiliationEditText;
    @BindView(R.id.forceSensitiveEditText)
    EditText forceSensitiveEditText;
    @BindView(R.id.takePictureFloatingActionButton)
    FloatingActionButton takePictureFloatingActionButton;
    @BindView(R.id.activityIndividualEditImage)
    ImageView activityIndividualEditImage;

    ListPopupWindow affiliationItemList;
    String[] affiliations = {"JEDI", "SITH", "RESISTANCE","FIRST ORDER"};
    ListPopupWindow forceSensitiveItemList;
    String[] forceSensitive = {"YES", "NO"};
    private Uri imageUri;
    long individualId;
    private Menu collapsedMenu;
    private boolean appBarExpanded = true;

    @Inject
    IndividualEditPresenter presenter;
    @Inject
    InternalIntents internalIntents;

    public IndividualEditActivity() {
        Injector.get().inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getApplication().getSharedPreferences(AppConstants.MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_individual_edit);
        ButterKnife.bind(this);
        PocketKnife.bindExtras(this);

        setupActionBar();

        individualId = getIntent().getLongExtra(IndividualContract.Extras.EXTRA_ID, 0L);

        presenter.init(this, individualId);
        presenter.load();
    }

    private void setupActionBar() {
        appbar.addOnOffsetChangedListener(this);
        setSupportActionBar(toolbar);
        enableActionBarBackArrow();
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Edit");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.individual_edit_menu, menu);
        collapsedMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null) {
            return false;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_item_save:
                presenter.saveIndividual();
                return true;
            case R.id.menu_item_photo:
                onTakePictureClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.birthDateEditText)
    public void onBirthdayClick() {
        presenter.birthdayClicked();
    }

    @OnClick(R.id.affiliationEditText)
    public void onAffiliationClick(){
        presenter.affiliationClicked();
    }

    @OnClick(R.id.forceSensitiveEditText)
    public void onForceSensitiveClick(){
        presenter.forceSensitiveClick();
    }

    @OnFocusChange(R.id.birthDateEditText)
    public void onBirthdayFocus(){
        if(birthDateEditText.isFocused())
            presenter.birthdayClicked();
    }

    @OnFocusChange(R.id.affiliationEditText)
    public void onAffiliationFocus(){
        if(affiliationEditText.isFocused())
            presenter.affiliationClicked();
    }

    @OnFocusChange(R.id.forceSensitiveEditText)
    public void onForceSensitiveFocus(){
        if(forceSensitiveEditText.isFocused())
            presenter.forceSensitiveClick();
    }

    @Override
    public void showBirthDateSelector(LocalDate date) {
        DialogFragment newFragment = new DatePickerFragment();
        presenter.hideKeyboard(this);
        birthDateEditText.setInputType(InputType.TYPE_NULL);
        newFragment.show(getFragmentManager(), TAG);
    }

    public void showIndividual(Individual individual) {
        firstNameEditText.setText(individual.getFirstName());
        lastNameEditText.setText(individual.getLastName());
        if(individual.getBirthDate() != null){
            showBirthDate(individual.getBirthDate());
        }
        affiliationEditText.setText(individual.getAffiliation());
        if(individual.isForceSensitive()){
            forceSensitiveEditText.setText("YES");
        }else{
            forceSensitiveEditText.setText("NO");
        }
        if(!individual.getProfilePicture().isEmpty()){
            Picasso.with(this).load(individual.getProfilePicture()).placeholder(R.drawable.default_image).into(activityIndividualEditImage);
        }else{
            Picasso.with(this).load(R.drawable.default_image).placeholder(R.drawable.default_image).into(activityIndividualEditImage);
        }
    }

    public void showBirthDate(LocalDate date) {
        long millis = DBToolsThreeTenFormatter.localDateTimeToLong(date.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());
        birthDateEditText.setText(DateUtils.formatDateTime(this, millis, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    @Override
    public void showForceSensitiveSelector() {
        forceSensitiveItemList = new ListPopupWindow(this);
        forceSensitiveItemList.setAdapter(new ArrayAdapter<>(this, R.layout.force_sensitive_item, forceSensitive));
        forceSensitiveItemList.setAnchorView(forceSensitiveEditText);
        forceSensitiveItemList.setWidth(ListPopupWindow.WRAP_CONTENT);
        forceSensitiveItemList.setHeight(ListPopupWindow.WRAP_CONTENT);
        forceSensitiveItemList.setModal(true);
        presenter.hideKeyboard(this);
        forceSensitiveEditText.setInputType(InputType.TYPE_NULL);
        forceSensitiveItemList.setOnItemClickListener((adapterView, view, i, l) -> {
            forceSensitiveEditText.setText(forceSensitive[i]);
            forceSensitiveItemList.dismiss();
        });
        forceSensitiveItemList.show();
    }

    @Override
    public void showAffiliationSelector() {
        affiliationItemList = new ListPopupWindow(this);
        affiliationItemList.setAdapter(new ArrayAdapter<>(this, R.layout.affiliation_item, affiliations));
        affiliationItemList.setAnchorView(affiliationEditText);
        affiliationItemList.setWidth(ListPopupWindow.WRAP_CONTENT);
        affiliationItemList.setHeight(ListPopupWindow.WRAP_CONTENT);
        affiliationItemList.setModal(true);
        presenter.hideKeyboard(this);
        affiliationEditText.setInputType(InputType.TYPE_NULL);
        affiliationItemList.setOnItemClickListener((adapterView, view, i, l) -> {
            affiliationEditText.setText(affiliations[i]);
            affiliationItemList.dismiss();
        });
        affiliationItemList.show();
    }

    @Override
    public void showProfilePicture(Uri uri) {
        Picasso.with(this).load(uri).placeholder(R.drawable.default_image).error(R.drawable.splash_image).into(activityIndividualEditImage);
        imageUri = uri;
    }

    public boolean validateIndividualData() {
        if (StringUtils.isBlank(firstNameEditText.getText())) {
            firstNameLayout.setError(getString(R.string.required));
            return false;
        }

        return true;
    }

    public void getIndividualDataFromUi(Individual individual) {
        individual.setFirstName(firstNameEditText.getText().toString());
        individual.setLastName(lastNameEditText.getText().toString());
        individual.setAffiliation(individual.getAffiliation().toString().replace(" ", "_"));
        if(forceSensitiveEditText.getText().toString().equalsIgnoreCase("YES")){
            individual.setForceSensitive(true);
        }else{
            individual.setForceSensitive(false);
        }
        if(imageUri != null) {
            individual.setProfilePicture(imageUri.toString());
        }
    }

    @Override
    public void close() {
        finish();
    }

    @OnClick(R.id.takePictureFloatingActionButton)
    public void onTakePictureClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.getApplication().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        AppConstants.MY_REQUEST_CODE);
            }

            if(this.getApplication().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                internalIntents.takePicture(this, individualId);
            }else{
                Toast.makeText(this, "You have to give permission to use the camera", Toast.LENGTH_SHORT).show();
            }
        }else{
            internalIntents.takePicture(this, individualId);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = Uri.parse(sharedPreferences.getString(AppConstants.INDIVIDUAL_PROFILE_URI, ""));
                    presenter.pictureTaken(uri);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppConstants.MY_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                internalIntents.takePicture(this, individualId);
            }
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (Math.abs(verticalOffset) > 200) {
            appBarExpanded = false;
            invalidateOptionsMenu();
        } else {
            appBarExpanded = true;
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (collapsedMenu != null
                && (!appBarExpanded || collapsedMenu.size() != 1)) {
            collapsedMenu.add(0, R.id.menu_item_photo, 0, R.string.photo)
                    .setIcon(R.drawable.ic_add_a_photo_white_24dp)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {

        }
        return super.onPrepareOptionsMenu(collapsedMenu);
    }
}