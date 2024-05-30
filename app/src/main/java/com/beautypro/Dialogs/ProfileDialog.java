package com.beautypro.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.beautypro.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.beautypro.Activities.AuthenticationActivity;
import com.beautypro.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileDialog extends AppCompatDialogFragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;
    Query qryUser;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    ConstraintLayout clEditProfile;
    TextView tvFullname, tvEmail;
    TextInputEditText etFirstName, etLastName, etMobile, etAddressPurok, etAddressBarangay, etAddressMunicipality;
    MaterialButton btnEditProfile, btnUpdateProfile, btnLogout;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_profile, null);

        initializeFirebase();
        initiate(view);
        loadUserInformation();
        buttonHandler();

        builder.setView(view);
        return builder.create();
    }

    private void initiate(View view) {
        clEditProfile = view.findViewById(R.id.clEditProfile);
        tvFullname = view.findViewById(R.id.tvFullname);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etMobile = view.findViewById(R.id.etMobile);
        etAddressPurok = view.findViewById(R.id.etAddressPurok);
        etAddressBarangay = view.findViewById(R.id.etAddressBarangay);
        etAddressMunicipality = view.findViewById(R.id.etAddressMunicipality);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
    }

    private void loadUserInformation() {
        DB.collection("users").document(AUTH.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String mobile = "";
                        String firstName = "";
                        String lastName = "";
                        String purok = "";
                        String barangay = "";

                        if (documentSnapshot.getData() != null){
                            firstName = (String) documentSnapshot.getData().get("firstName");
                            lastName = (String) documentSnapshot.getData().get("lastName");
                            mobile = (String) documentSnapshot.getData().get("mobile");
                            purok = (String) documentSnapshot.getData().get("addressPurok");
                            barangay = (String) documentSnapshot.getData().get("addressBarangay");
                            String fullName = firstName+" "+lastName;
                            tvFullname.setText(Utils.capitalizeEachWord(fullName.trim()));
                        }
                        else {
                            tvFullname.setText("No Name");
                        }

                        tvEmail.setText(AUTH.getCurrentUser().getEmail());
                        etFirstName.setText(firstName);
                        etLastName.setText(lastName);
                        etMobile.setText(mobile);
                        etAddressPurok.setText(purok);
                        etAddressBarangay.setText(barangay);
                    }
                });
    }

    private void buttonHandler() {
        btnLogout.setOnClickListener(view -> {
            MaterialAlertDialogBuilder dialogLogout = new MaterialAlertDialogBuilder(requireActivity());
            dialogLogout.setTitle("Confirm logout")
                    .setNeutralButton("Cancel", (dialogInterface, i) -> {

                    })
                    .setNegativeButton("Log out", (dialogInterface, i) -> {
                        AUTH.signOut();
                        startActivity(new Intent(requireContext(), AuthenticationActivity.class));
                        requireActivity().finish();
                    }).show();
        });

        btnEditProfile.setOnClickListener(view -> {
            btnEditProfile.setVisibility(View.GONE);
            clEditProfile.setVisibility(View.VISIBLE);
        });

        btnUpdateProfile.setOnClickListener(view -> {
            btnUpdateProfile.setEnabled(false);

            String firstName = etFirstName.getText().toString();
            String lastName = etLastName.getText().toString();
            String mobile = etMobile.getText().toString();
            String purok = etAddressPurok.getText().toString();
            String barangay = etAddressBarangay.getText().toString();

            if (firstName.isEmpty() ||
                lastName.isEmpty() ||
                mobile.isEmpty() ||
                purok.isEmpty() ||
                barangay.isEmpty())
            {
                Utils.simpleDialog(requireContext(), "Failed to Update Profile", "All fields cannot be left empty. Please fill in all the fields to update your account information.", "Okay");
                btnUpdateProfile.setEnabled(true);
                return;
            }

            Map<String, Object> user = new HashMap<>();
            user.put("firstName", Objects.requireNonNull(firstName));
            user.put("lastName", Objects.requireNonNull(lastName));
            user.put("mobile", Objects.requireNonNull(mobile));
            user.put("addressPurok", Objects.requireNonNull(purok));
            user.put("addressBarangay", Objects.requireNonNull(barangay));

            DB.collection("users").document(AUTH.getCurrentUser().getUid())
                    .set(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                requireDialog().dismiss();
                                Toast.makeText(requireContext(), "Updated Profile", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }
}
