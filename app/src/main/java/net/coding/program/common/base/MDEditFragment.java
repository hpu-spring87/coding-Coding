package net.coding.program.common.base;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.ListModify;
import net.coding.program.common.PhotoOperate;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.common.photopick.CameraPhotoUtil;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.project.detail.TopicEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

@EFragment(R.layout.fragment_mdedit)
public class MDEditFragment extends BaseFragment {

    @ViewById
    protected EditText edit;

    private Uri fileUri;
    private Uri fileCropUri;

    private final String tipFont = "在此输入文字";
//    private final String host_upload_photo = "https://coding.net/api/project/%d/file/upload";

    private final String HOST_UPLOAD_PHOTO_PUBLIC = Global.HOST + "/api/project/%d/upload_public_image";
    private final String HOST_UPLOAD_PHOTO_PRIVATE = Global.HOST + "/api/project/%d/file/upload";

    private String hostUploadPhoto = "";

    @AfterViews
    protected final void initBase1() {
        TopicEditFragment.SaveData projectData = (TopicEditFragment.SaveData) getActivity();
        int projectId = projectData.getProjectId();
        if (projectData.isProjectPublic()) {
            hostUploadPhoto = String.format(HOST_UPLOAD_PHOTO_PUBLIC, projectId);
        } else {
            hostUploadPhoto = String.format(HOST_UPLOAD_PHOTO_PRIVATE, projectId);
        }
    }

    @Click
    public void mdBold(View v) {
        insertString(" **", tipFont, "** ");
    }

    @Click
    public void mdItalic(View v) {
        insertString(" *", tipFont, "* ");
    }

    @Click
    public void mdHyperlink(View view) {
        insertString("[", tipFont, "]()");
    }

    @Click
    public void mdLinkQuote(View view) {
        insertString("\n> ", tipFont, "");
    }

    @Click
    public void mdCode(View v) {
        insertString("\n```\n",
                tipFont,
                "\n```\n");
    }

    @Click
    public void mdTitle(View view) {
        insertString("## ", tipFont, " ##");
    }

    @Click
    public void mdList(View v) {
        insertString("\n - ", tipFont, "");
    }

    @Click
    public void mdDivide(View v) {
        insertString("\n----------\n", tipFont, "");
    }


    @Click
    public void mdPhoto(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("上传图片")
                .setItems(R.array.camera_gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            camera();
                        } else {
                            photo();
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        CustomDialog.dialogTitleLineColor(getActivity(), dialog);
    }

    private void camera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = CameraPhotoUtil.getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, RESULT_REQUEST_PHOTO);
    }

    private void photo() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_REQUEST_PHOTO);
    }

    private final int RESULT_REQUEST_PHOTO = 1005;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    if (data != null) {
                        showProgressBar(true, "正在上传图片...");
                        setProgressBarProgress(30);
                        fileUri = data.getData();
                        File outputFile = new PhotoOperate(getActivity()).scal(fileUri);


                        RequestParams params = new RequestParams();
                        params.put("dir", 0);
                        params.put("file", outputFile);
                        postNetwork(hostUploadPhoto, params, hostUploadPhoto);
                    }
                } catch (Exception e) {
                    showProgressBar(false);
                }
            }
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(hostUploadPhoto)) {
            showProgressBar(false);
            if (code == 0) {
                String fileUri;
                if (((TopicEditFragment.SaveData) getActivity()).isProjectPublic()) {
                    fileUri = respanse.optString("data", "");
                } else {
                    AttachmentFileObject fileObject = new AttachmentFileObject(respanse.optJSONObject("data"));
                    fileUri = fileObject.owner_preview;
                }
                String mdPhotoUri = String.format("![图片](%s)\n", fileUri);
                insertString(mdPhotoUri, "", "");
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void insertString(String begin, String middle, String end) {
        edit.requestFocus();
        Global.popSoftkeyboard(getActivity(), edit, true);

        String insertString = String.format("%s%s%s", begin, middle, end);
        int insertPos = edit.getSelectionStart();
        int selectBegin = insertPos - begin.length();
        int selectEnd = selectBegin + insertString.length();

        Editable editable = edit.getText();
        String currentInput = editable.toString();

        if (0 <= selectBegin &&
                selectEnd <= currentInput.length() &&
                insertString.equals(currentInput.substring(selectBegin, selectEnd))) { //
            editable.replace(selectBegin, selectEnd, middle);
            edit.setSelection(selectBegin, selectBegin + middle.length());
        } else {
            editable.replace(insertPos, edit.getSelectionEnd(), insertString);
            edit.setSelection(insertPos + begin.length(), insertPos + begin.length() + middle.length());
        }
    }
}
