package com.example.administrator.myapplication;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {
    Context context;
    LayoutInflater inflater;
    Handler handler;
    ArrayList<Map<String, Object>> group;
    ArrayList<ArrayList<Map<String, Object>>> child;
    Map<String, Object> map;
    private  static MyExpandableListAdapter myAdapter=null;

    public  static MyExpandableListAdapter getInstance(Context context, ArrayList<Map<String, Object>>group, ArrayList<ArrayList<Map<String, Object>>>child){
        if(myAdapter==null){
            myAdapter=new MyExpandableListAdapter(context,group,child);
        }
        return myAdapter;
    }
    public MyExpandableListAdapter(Context context, ArrayList<Map<String, Object>>group, ArrayList<ArrayList<Map<String, Object>>>child){
        this.context = context;

        inflater = LayoutInflater.from(context);
        this.group = group;
        this.child = child;

/*
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                     notifyDataSetChanged();
                     super.handleMessage(msg);
            }
        };*/


    }

    public void refresh(){
          notifyDataSetChanged();
        //  handler.sendMessage(new Message());
        //必须重新伸缩之后才能更新数据

    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        /*child.get(groupPosition)是得到groupPosition处的list对象，然后
         * child.get(groupPosition).get(childPosition)得到child的map对象，然后
         * child.get(groupPosition).get(childPosition).get("Child")是得到key值
         * 为Child的值
         * */

        return child.get(groupPosition).get(childPosition).get("child");
    }
    /**
     * 获取指定组中的指定子元素ID，这个ID在组里一定是唯一的。联合ID（getCombinedChildId(long, long)）在所有条目（所有组和所有元素）中也是唯一的。
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        /******子元素的位置********/
        return childPosition;
    }
    /**获取一个视图对象，显示指定组中的指定子元素数据。
     * @param groupPosition 组位置（该组内部含有子元素）
     * @param childPosition 子元素位置（决定返回哪个视图）
     * @param isLastChild 子元素是否处于组中的最后一个
     * @param convertView 重用已经有的视图对象，它是RecycleBin缓存机制调用getScrapView方法获取废弃已缓存的view.
     * @param parent 返回的视图(View)对象始终依附于的视图组。通俗的说是它的父视图。
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolderChild viewHolderChild;
        /*当convertView为空，也就是没有废弃已缓存 view时，将执行下面方法，调用layoutinflate的
         * inflate()方法来加载一个view。
         * 如有不懂，请点击：http://blog.csdn.net/libmill/article/details/49644743
         */
        if(convertView==null){
            //重新加载布局
            convertView = inflater.inflate(R.layout.child, null);
            //初始化控件管理器（自己命名的）
            viewHolderChild = new ViewHolderChild();
            //绑定控件id
            viewHolderChild.tv = (TextView) convertView.findViewById(R.id.tv);
            /*convertView的setTag将viewHolderChild设置到Tag中，以便系统第二次绘制
                ExpandableListView时从Tag中取出
            */
            convertView.setTag(viewHolderChild);
        }else{
            //当convertView不为空时，从Tag中取出viewHolderChild
            viewHolderChild = (ViewHolderChild) convertView.getTag();
        }
        //给子元素的TextView设置值
        viewHolderChild.tv.setText(getChild(groupPosition, childPosition).toString());
        //返回视图对象，这里是childPostion处的视图
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return child.get(groupPosition).size();
    }

    @Override
    public long getCombinedChildId(long groupId , long childId ) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {

        return group.get(groupPosition).get("group");
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return group.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolderGroup viewHolder;
        //判断convertView是否为空，convertView是RecycleBean调用getScrapView函数得到废弃已缓存的view
        if(convertView==null){
            //初始化控件管理器对象
            viewHolder = new ViewHolderGroup();
            //重新加载布局
            convertView = inflater.inflate(R.layout.group, null);
            //给组元素绑定ID
            viewHolder.logo_tv = (TextView) convertView.findViewById(R.id.logo);
            //给组元素箭头绑定ID
            viewHolder.arrow = (ImageView) convertView.findViewById(R.id.arrow);
            //convertView将viewHolder设置到Tag中，以便再次绘制ExpandableListView时从Tag中取出viewHolder;
            convertView.setTag(viewHolder);
        }else {//如果convertView不为空，即getScrapView得到废弃已缓存的view
            //从Tag中取出之前存入的viewHolder
            viewHolder = (ViewHolderGroup) convertView.getTag();
        }
        //设置组值
        viewHolder.logo_tv.setText(getGroup(groupPosition).toString());
        //如果组是展开状态
        if (isExpanded) {
            //箭头向下
            viewHolder.arrow.setImageResource(R.drawable.arrow_down);
        }else{//如果组是伸缩状态
            //箭头向右
            viewHolder.arrow.setImageResource(R.drawable.arrow);
        }
        //返回得到的指定组的视图对象
        return convertView;
    }
    /**
     * 组和子元素是否持有稳定的ID,也就是底层数据的改变不会影响到它们。
     * @return 返回一个Boolean类型的值，如果为TRUE，意味着相同的ID永远引用相同的对象
     */
    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }
    /**
     * 是否选中指定位置上的子元素。
     */
    @Override
    public boolean isChildSelectable(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return true;
    }
    /**
     * 如果当前适配器不包含任何数据则返回True。经常用来决定一个空视图是否应该被显示。
     * 一个典型的实现将返回表达式getCount() == 0的结果，但是由于getCount()包含了头部和尾部，适配器可能需要不同的行为。
     */
    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }
    /**
     * 当组收缩状态的时候此方法被调用。
     */
    @Override
    public void onGroupCollapsed(int groupPosition) {
        // TODO Auto-generated method stub

    }
    /**
     * 当组展开状态的时候此方法被调用。
     */
    @Override
    public void onGroupExpanded(int groupPosition) {
        // TODO Auto-generated method stub

    }
    /**
     * 注册一个观察者(observer)，当此适配器数据修改时即调用此观察者。
     * @param observer:当数据修改时通知调用的对象
     */
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        // TODO Auto-generated method stub

    }
    /**
     * 取消先前通过registerDataSetObserver(DataSetObserver)方式注册进该适配器中的观察者对象。
     * @param observer 取消这个观察者的注册
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // TODO Auto-generated method stub

    }
    /**
     * 组控件管理器
     * @author Administrator
     *
     */
    class ViewHolderGroup{
        TextView logo_tv;
        ImageView arrow;
    }
    class ViewHolderChild{
        TextView tv;
    }
}