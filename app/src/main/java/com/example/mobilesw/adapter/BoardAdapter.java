// 게시판의 카드뷰 생성, 게시글 생성, 수정, 삭제와 연결됨

package com.example.mobilesw.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobilesw.activity.MainActivity;
import com.example.mobilesw.info.PostInfo;
import com.example.mobilesw.R;
import com.example.mobilesw.activity.BoardActivity;
import com.example.mobilesw.activity.PostActivity;
import com.example.mobilesw.view.ReadContentsView;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static com.example.mobilesw.info.Util.isStorageUrl;
import static com.example.mobilesw.info.Util.makeDialog;
import static com.example.mobilesw.info.Util.showToast;
import static com.example.mobilesw.info.Util.storageUrlToName;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.MainViewHolder> {
    private ArrayList<PostInfo> mDataset;
    private Activity activity;
    private ArrayList<ArrayList<SimpleExoPlayer>> playerArrayListArrayList = new ArrayList<>();
    private final int MORE_INDEX = 2;
    private int successCount;
    String profilePath;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    static class MainViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        MainViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public BoardAdapter(Activity activity, ArrayList<PostInfo> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @NonNull
    @Override
    public BoardAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_board, parent, false);
        final MainViewHolder mainViewHolder = new MainViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, BoardActivity.class);
                intent.putExtra("postInfo", mDataset.get(mainViewHolder.getAdapterPosition()));
                activity.startActivity(intent);
            }
        });

        cardView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, mainViewHolder.getAdapterPosition());
            }
        });

        return mainViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MainViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        TextView titleTextView = cardView.findViewById(R.id.titleTextView);
        ImageView profileView = cardView.findViewById(R.id.userProfile);
        TextView userName = cardView.findViewById(R.id.userName);


        PostInfo postInfo = mDataset.get(position);

        // firestore의 "users"에서 이름과 프로필 사진을 가져옴
        db.collection("users").document(postInfo.getPublisher()).
            get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.get("name").toString();
                        userName.setText(name);

                        if(document.get("profilePath")!=null) {
                            profilePath = document.get("profilePath").toString();
                            Glide.with(profileView).load(profilePath).centerCrop().override(500).into(profileView);
                        }
                        titleTextView.setText(postInfo.getTitle());

                        ReadContentsView readContentsVIew = cardView.findViewById(R.id.readContentsView);
                        LinearLayout contentsLayout = cardView.findViewById(R.id.contentsLayout);

                        if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(postInfo)) {
                            contentsLayout.setTag(postInfo);
                            contentsLayout.removeAllViews();

                            readContentsVIew.setMoreIndex(MORE_INDEX);
                            readContentsVIew.setPostInfo(postInfo);

                            ArrayList<SimpleExoPlayer> playerArrayList = readContentsVIew.getPlayerArrayList();
                            if(playerArrayList != null){
                                playerArrayListArrayList.add(playerArrayList);
                            }
                        }
                    }
                }
            }
        });;


    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private void showPopup(View v, final int position) {
        PopupMenu popup = new PopupMenu(activity, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.modify:
                        myStartActivity(PostActivity.class, mDataset.get(position));
                        return true;
                    case R.id.delete:
                        storageDelete(mDataset.get(position));
                        return true;
                    default:
                        return false;
                }
            }
        });

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.post_menu, popup.getMenu());
        popup.show();
    }

    private void myStartActivity(Class c, PostInfo postInfo) {
        Intent intent = new Intent(activity, c);
        intent.putExtra("postInfo", postInfo);
        activity.startActivity(intent);
    }

    public void playerStop(){
        for(int i = 0; i < playerArrayListArrayList.size(); i++){
            ArrayList<SimpleExoPlayer> playerArrayList = playerArrayListArrayList.get(i);
            for(int ii = 0; ii < playerArrayList.size(); ii++){
                SimpleExoPlayer player = playerArrayList.get(ii);
                if(player.getPlayWhenReady()){
                    player.setPlayWhenReady(false);
                }
            }
        }
    }

    public void storageDelete(final PostInfo postInfo){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        final String id = postInfo.getId();
        ArrayList<String> contentsList = postInfo.getContents();
        for (int i = 0; i < contentsList.size(); i++) {
            String contents = contentsList.get(i);
            if (isStorageUrl(contents)) {
                successCount++;
                System.out.println("successCount");
                StorageReference desertRef = storageRef.child("posts/" + id + "/" + storageUrlToName(contents));
                System.out.println("urlName: "+storageUrlToName(contents));
                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        successCount--;
                        storeDelete(id, postInfo);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        showToast(activity, "게시물을 삭제하지 못했습니다. 다시 시도해주세요");
                    }
                });
            }
        }
        storeDelete(id, postInfo);
    }


    private void storeDelete(final String id, final PostInfo postInfo) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (successCount == 0) {
            firebaseFirestore.collection("posts").document(id)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showToast(activity, "게시물을 삭제하였습니다.");
                            Intent intent= new Intent(activity, MainActivity.class);;
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra("fragnum",3);
                            activity.startActivity(intent);

                            //postsUpdate();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showToast(activity, "게시물을 삭제하지 못했습니다. 다시 시도해주세요");
                        }
                    });
        }
    }
}