package com.example.smartchatters.View;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

//import com.example.smartchatters.Model.ItemViewModel;
import com.example.smartchatters.R;
import com.example.smartchatters.logic.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFragment extends Fragment {
//    private ItemViewModel viewModel;
    private CustomAdapter adapter;
    private Usernode user;
    private ExecutorService executorService;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = Singleton.getInstance().getUser();
//        viewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
//        viewModel.getSelectedItem().observe(this, item -> {
//           user=viewModel.getSelectedItem().getValue();
//        });


        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        List<String> items=new LinkedList<>();
        if (Usernode.topicNames!=null){
            for (int i=0; i<Usernode.topicNames.size() ; i++) {
                items.add(Usernode.topicNames.get(i));
            }
        }


        adapter = new CustomAdapter(items);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_main, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.chatsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.search_filter, menu);
        MenuItem item= menu.findItem(R.id.searchFilter);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> implements Filterable {

        List<String> items;
        List<String> chatsItemsAll;

        public CustomAdapter(List<String> items){
            this.items=items;
            this.chatsItemsAll=new ArrayList<>(items);
        }
        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,parent,false);
            return new CustomViewHolder(view).linkAdapter(this);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
            holder.textView.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        Filter filter=new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                List<String> filteredList = new ArrayList<>();

                if (charSequence.toString().isEmpty())filteredList.addAll(chatsItemsAll);
                else {
                    for (String chatName: chatsItemsAll){
                        if (chatName.toLowerCase().contains(charSequence.toString().toLowerCase()))filteredList.add(chatName);
                    }
                }

                FilterResults filterResults=new FilterResults();
                filterResults.values=filteredList;
                filterResults.count = filteredList.size();

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                items.clear();
                items.addAll((Collection<? extends String>) filterResults.values);
                notifyItemInserted(items.size()-1);
                notifyDataSetChanged();

            }
        };

        @Override
        public Filter getFilter() {
            return filter;
        }

    }

    class CustomViewHolder extends RecyclerView.ViewHolder{

        private TextView textView;
        private CustomAdapter adapter;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            textView=itemView.findViewById(R.id.textViewChatCard);
            Singleton singleton=Singleton.getInstance();
            executorService = singleton.getExecutorService();
            itemView.findViewById(R.id.joinButtonChatCard).setOnClickListener(view -> {
//                viewModel.getSelectedItem().observe(getViewLifecycleOwner(), item -> {
//                    System.out.println(textView.getText().toString());
//                });
                user = Singleton.getInstance().getUser();
                registerThread(textView.getText().toString());
                Intent chatIntent=new Intent(itemView.getContext(), ChatActivity.class);
                chatIntent.putExtra("chatName",textView.getText());

                itemView.getContext().startActivity(chatIntent);
            });
        }

        public CustomViewHolder linkAdapter(CustomAdapter adapter){
            this.adapter=adapter;
            return this;
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
//    protected class AsyncRegister extends AsyncTask<Void,Void,Void > {
//
//        String topicName;
//
//        public AsyncRegister(String topicName){
//            this.topicName=topicName;
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            if (user.checkSubscriptionToTopic(topicName)){
//                //System.out.println("Already registered to "+topicName);
//                return null;
//            }
//            user.register(topicName);
//            return null;
//        }
//    }

        public void registerThread(String topicName) {
            executorService.execute(new Runnable() {
                public void run() {
                    for (String topic : Singleton.getInstance().getNeedRestart().keySet()) {
                        if (topic.equals(topicName)) {
                            user.startConsuming(topicName,Singleton.getInstance().getCounter(topicName));
                            Singleton.getInstance().getNeedRestart().remove(topicName);
                        }
                    }

                    if (user.checkSubscriptionToTopic(topicName)){

                        return;
                    //System.out.println("Already registered to "+topicName);
                }
                    System.out.println("GEIAAA2 "+topicName);
                    user.register(topicName);
                    return;
                }
            });
    }

}

