package com.hgxx.whiteboard.views;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hgxx.whiteboard.R;
import com.hgxx.whiteboard.WhiteBoardApplication;
import com.hgxx.whiteboard.models.PresentationInfo;

/**
 * Created by ly on 24/05/2017.
 */

public class PresentationSelectFragment extends Fragment{

    private View contentView;
    private TextView title;
    private String titleText;
    private ImageView backBtn;
    private PresentationAdapter presentationAdapter;
    private ListView list;
    private BaseAdapter adapter;
    private boolean backEnabled = true;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.select_presentation_fragment, null);
        findViews();
        initViews();
        return contentView;

    }

    @Override
    public void onResume() {
        super.onResume();
        initViewDatas();
    }

    private void findViews(){
        title = (TextView)contentView.findViewById(R.id.bgsel_title_text);
        backBtn = (ImageView)contentView.findViewById(R.id.bgsel_title_back_btn);
        list = (ListView)contentView.findViewById(R.id.presentation_list);
    }

    public interface OnBackPressed{
        void onBackPressed();
    }
    private OnBackPressed onBackPressed;

    /**
     * public interfaces
     */
    public void notifyDataChanges(){
        setTitle(titleText);
        adapter.notifyDataSetChanged();
    }

    public void setPresentationAdapter(PresentationAdapter presentationAdapter) {
        this.presentationAdapter = presentationAdapter;
        if(adapter!=null){
            notifyDataChanges();
        }
    }
    public void setOnBackPressed(OnBackPressed onBackPressed) {
        this.onBackPressed = onBackPressed;
    }
    public void setTitle(String title){
        this.titleText = title;
        if(adapter!=null){
            notifyDataChanges();
        }
    }


    public void setCanReturn(boolean canReturn){
        backEnabled=canReturn;
    }

    private boolean getCanReturn(){
        return backEnabled;
    }

    private void initViews(){

        title.setText(titleText);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!getCanReturn())return;
                if(onBackPressed!=null){
                    onBackPressed.onBackPressed();
                }
                closeSelf();
            }
        });
    }




    private void closeSelf(){
        if(getActivity()!=null){
            FragmentManager fm = getActivity().getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(this);
            ft.commit();
        }else{
            ((ViewGroup)contentView.getParent()).removeView(contentView);
        }

        if(onPresentationSelectPageClose!=null){
            onPresentationSelectPageClose.onPresentationSelectPageClose();
        }

    }

    private void initViewDatas(){
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return presentationAdapter==null?1:presentationAdapter.getCount()+1;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {


                View v;
                ViewHolder vh;
                if(convertView!=null){
                    v=convertView;
                    vh=(ViewHolder)v.getTag();
                    clearInfo(vh);
                }
                else{
                    v=View.inflate(getActivity(), R.layout.presentation_cell, null);
                    vh = new ViewHolder();
                    setTag(v, vh);
                }

                fillContent(vh, position);

                return v;
            }
        };
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PresentationInfo pi=null;
                if(position==presentationAdapter.getCount()){
                    pi = new PresentationInfo("白板");
                    pi.setPresentationId("-1");
                    pi.setCount(1);
                }
                else{
                    pi = presentationAdapter.getPresentationInfo(position);
                }

                if(onPresentationSelectPageClose !=null){
                    onPresentationSelectPageClose.onPresentationSelected(pi);
                }
                closeSelf();
            }
        });
    }

    public interface OnPresentationSelectPageClose {
        void onPresentationSelected(PresentationInfo pi);
        void onPresentationSelectPageClose();
    }

    private OnPresentationSelectPageClose onPresentationSelectPageClose;

    public void setOnPresentationSelectPageClose(OnPresentationSelectPageClose onPresentationSelectPageClose) {
        this.onPresentationSelectPageClose = onPresentationSelectPageClose;
    }

    private void clearInfo(ViewHolder vh){
        if(vh==null)return;
        vh.thumb.setImageResource(R.drawable.openfile);
        vh.title.setText("");
        vh.info.setText("");
    }

    private void setTag(View v, ViewHolder vh) {
        vh.thumb = (ImageView)v.findViewById(R.id.presentation_thumb);
        vh.title = (TextView)v.findViewById(R.id.presentation_title);
        vh.info = (TextView)v.findViewById(R.id.presentation_info);
        v.setTag(vh);
    }

    class ViewHolder{
        ImageView thumb;
        TextView title;
        TextView info;
    }

    private void fillContent(ViewHolder vh, int position){

        if(presentationAdapter==null||position==presentationAdapter.getCount()){
            vh.thumb.setImageResource(R.drawable.openfile);
            vh.title.setText("白板");
            return;
        }

        PresentationInfo pi = presentationAdapter.getPresentationInfo(position);
        if(pi==null)return;

        if(pi.getThumb()!=null){
            vh.thumb.setImageBitmap(pi.getThumb());
        }
        else{
            vh.thumb.setImageResource(R.drawable.openfile);
        }

        if(!TextUtils.isEmpty(pi.getPresentationName())){
            vh.title.setText(pi.getPresentationName());
        }

        String info = "";
        if(!TextUtils.isEmpty(pi.getUploadTime())){
            info+=pi.getUploadTime()+", ";
        }

        if(!TextUtils.isEmpty(pi.getSize())){
            info+=pi.getSize();
        }
        vh.info.setText(info);
    }


    /**
     * getters & setters
     */

    public PresentationAdapter getAdapter() {
        return presentationAdapter;
    }

}
