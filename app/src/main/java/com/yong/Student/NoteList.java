package com.yong.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class NoteList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Note List");
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

        }

        FirebaseApp.initializeApp(NoteList.this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        FloatingActionButton add = findViewById(R.id.addNote);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view1 = LayoutInflater.from(NoteList.this).inflate(R.layout.add_note_dialog, null);
                TextInputLayout titleLayout, contentLayout;
                titleLayout = view1.findViewById(R.id.titleLayout);
                contentLayout = view1.findViewById(R.id.conntentLayout);

                TextInputEditText titleET, contentET;
                titleET = view1.findViewById(R.id.titleET);
                contentET = view1.findViewById(R.id.contentET);

                AlertDialog alertDialog = new AlertDialog.Builder(NoteList.this)
                        .setTitle("Add")
                        .setView(view1)
                        .setPositiveButton("add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                if (Objects.requireNonNull(titleET.getText()).toString().isEmpty()) {
                                    titleLayout.setError("This field ia required!");
                                } else if (Objects.requireNonNull(contentET.getText()).toString().isEmpty()) {
                                    contentLayout.setError("This field ia required!");
                                } else {
                                    ProgressDialog dialog1 = new ProgressDialog(NoteList.this);
                                    dialog1.setMessage("Storing in Database....");
                                    Note note = new Note();
                                    note.setTitle(titleET.getText().toString());
                                    note.setContent(contentET.getText().toString());
                                    database.getReference().child("noted").push().setValue(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            dialog1.dismiss();
                                            dialogInterface.dismiss();
                                            Toast.makeText(NoteList.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog1.dismiss();
                                            Toast.makeText(NoteList.this, "There was erorr while saving data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });

        TextView empty = findViewById(R.id.empty);
        ImageView imgEmpty = findViewById(R.id.img_empty);

        RecyclerView recyclerView = findViewById(R.id.recycler);


        // String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        database.getReference().child("noted")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<Note> arrayList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            Note note = dataSnapshot1.getValue(Note.class);
                            Objects.requireNonNull(note).setKey(dataSnapshot1.getKey());
                            arrayList.add(note);
                        }

                        //set visible emty text and image
                        if (arrayList.isEmpty()) {
                            empty.setVisibility(View.VISIBLE);
                            imgEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            empty.setVisibility(View.GONE);
                            imgEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        NoteAdapter adapter = new NoteAdapter(NoteList.this, arrayList);
                        recyclerView.setAdapter(adapter);

                        adapter.setOnItemClickListener(new NoteAdapter.onItemClickListener() {
                            @Override
                            public void onClick(Note note) {
                                View view = LayoutInflater.from(NoteList.this).inflate(R.layout.add_note_dialog, null);
                                TextInputLayout titleLayout, contentLayout;
                                TextInputEditText titleET, contentET;

                                titleET = view.findViewById(R.id.titleET);
                                contentET = view.findViewById(R.id.contentET);
                                titleLayout = view.findViewById(R.id.titleLayout);
                                contentLayout = view.findViewById(R.id.conntentLayout);

                                titleET.setText(note.getTitle());
                                contentET.setText(note.getContent());

                                ProgressDialog progressDialog = new ProgressDialog(NoteList.this);

                                AlertDialog alertDialog = new AlertDialog.Builder(NoteList.this)
                                        .setTitle("Edit")
                                        .setView(view)
                                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int which) {
                                                if (Objects.requireNonNull(titleET.getText()).toString().isEmpty()) {
                                                    titleLayout.setError("This field ia required!");
                                                } else if (Objects.requireNonNull(contentET.getText()).toString().isEmpty()) {
                                                    contentLayout.setError("This field ia required!");
                                                } else {
                                                    progressDialog.setMessage("Saving....");
                                                    progressDialog.show();
                                                    Note note1 = new Note();
                                                    note1.setTitle(titleET.getText().toString());
                                                    note1.setContent(contentET.getText().toString());
                                                    database.getReference().child("noted").child(note.getKey()).setValue(note1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            progressDialog.dismiss();
                                                            dialogInterface.dismiss();
                                                            Toast.makeText(NoteList.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(NoteList.this, "There was an error while saving data", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                                }
                                            }
                                        })
                                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int which) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(NoteList.this);
                                                builder.setTitle("Confirm Delete");
                                                builder.setMessage("Are you sure you want to delete this note?");
                                                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        progressDialog.setTitle("Deleting...");
                                                        progressDialog.show();
                                                        database.getReference().child("noted").child(note.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(NoteList.this, "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(NoteList.this, "There was an error while deleting data", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                builder.show();
                                            }
                                        }).create();
                                alertDialog.show();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // This is the ID for the back button in the action bar/toolbar
            // Navigate back to MainActivity
            onBackPressed(); // This will simulate the back button press
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
