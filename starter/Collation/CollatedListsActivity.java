package com.parse.starter.Collation;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.CollateActivity;
import com.parse.starter.R;

import java.util.ArrayList;
import java.util.List;

public class CollatedListsActivity extends AppCompatActivity {

    ArrayList<SavedList> savedLists;
    GridView collatedGrid;
    CollationListAdapterMain adapter;
    Button createButton;
    AlertDialog dialog;
    SavedList editSavedList;
    EditText title;
    List<String> collatedLists;
    Gson gson;
    SavedList savedList1;
    int index;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collated_lists);
        collatedGrid = (GridView)findViewById(R.id.collatedListGrid);
        setTitle("Collated top idea lists");
        gson = new Gson();
        queryLists();
    }

    private void queryLists() {
        savedLists = new ArrayList<>();
        collatedLists = ParseUser.getCurrentUser().getList("CollatedLists");
        for (String collatedList:collatedLists) {
           SavedList savedList = gson.fromJson(collatedList,SavedList.class);
           savedLists.add(savedList);
        }
        adapter = new CollationListAdapterMain(this,R.layout.collated_list_main,savedLists);
        collatedGrid.setAdapter(adapter);
        collatedGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("onItemClick",Integer.toString(position));
                gotoCollateList(position);
            }
        });
        Log.i("savedLists",savedLists.get(0).savedIds.toString());
        Log.i("savedListstitle",savedLists.get(0).author);
    }

    public void onClickEdit(View view){
        editSavedList = (SavedList)view.getTag();
        activatePopUp(editSavedList);
    }

    private void activatePopUp( SavedList savedList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.collate_edit_popup,null);
        title = (EditText)view.findViewById(R.id.editTitle);
        TextView author = (TextView)view.findViewById(R.id.editAuthor);
        title.setText(savedList.mainTitle);
        author.setText(savedList.author);
        createButton = (Button)view.findViewById(R.id.create);
        builder.setView(view);
        dialog = builder.create();
        builder.show();
    }

    public void onClickChangeTitle(View view){
        String json = gson.toJson(editSavedList);
        index = collatedLists.indexOf(json);
        collatedLists.remove(json);
        savedList1 = gson.fromJson(json,SavedList.class);
        savedList1.setMainTitle(title.getText().toString());
        savedLists.add(savedLists.indexOf(editSavedList),savedList1);
        savedLists.remove(editSavedList);
        adapter.notifyDataSetChanged();
        parseChange();
    }

    private void parseChange() {
        String savedIDJSON = gson.toJson(savedList1);
        collatedLists.add(index,savedIDJSON);
        ParseUser.getCurrentUser().put("CollatedLists",collatedLists);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    Toast.makeText(CollatedListsActivity.this, "Title amended", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("parseChange", e.getMessage());
                }
            }
        });
    }

    public void gotoCollateList(int pos){
        Intent intent =  new Intent(this, CollateActivity.class);
        intent.putStringArrayListExtra("savedIds",savedLists.get(pos).savedIds);
        intent.putExtra("title",savedLists.get(pos).mainTitle);
        Log.i("gotoCollateList", savedLists.get(pos).savedIds.toString());
        startActivity(intent);
    }

    public void onClickDeleteCollated(View view){
        SavedList deleteSavedList = (SavedList)view.getTag();
        savedLists.remove(deleteSavedList);
        adapter.notifyDataSetChanged();
        String jsonItem = gson.toJson(deleteSavedList);
        collatedLists.remove(jsonItem);
        ParseUser.getCurrentUser().put("CollatedLists",collatedLists);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    Toast.makeText(CollatedListsActivity.this, "List deleted.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("parseChange", e.getMessage());
                }
            }
        });
    }

}
