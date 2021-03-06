package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import butterknife.Bind;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.FoodUserClient;
import openfoodfacts.github.scrachx.openfood.views.ScannerFragmentActivity;

public class HomeFragment extends BaseFragment {

    @Bind(R.id.buttonScan) Button mButtonScan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SharedPreferences settings = getActivity().getSharedPreferences("login", 0);
        String loginS = settings.getString("user", "");
        String passS = settings.getString("pass", "");

        mButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ScannerFragmentActivity.class);
                startActivity(intent);
            }
        });

        if (!loginS.isEmpty() && !passS.isEmpty()) {
            RequestParams params = new RequestParams();
            params.put("user_id", loginS);
            params.put("password", passS);
            params.put(".submit", "Sign-in");

            FoodUserClient.post("/cgi/session.pl", params, new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                    SharedPreferences.Editor editor = settings.edit();
                    String htmlNoParsed = new String(responseBody);
                    if (htmlNoParsed.contains("Incorrect user name or password.") || htmlNoParsed.contains("See you soon!")) {
                        editor.putString("user", "");
                        editor.putString("pass", "");
                        editor.commit();
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.alert_dialog_warning_title)
                                .content(R.string.alert_dialog_warning_msg_user)
                                .positiveText(R.string.txtOk)
                                .show();
                    }
                }

                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

                }

                @Override
                public void onFinish() {
                    super.onFinish();
                }
            });
        }
    }
}
