package com.haozhen.refreshView;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.haozhen.refreshlistview_library.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshHeadTailListView extends ListView {

	//因为头部要放多个 所以不能简单的定成View类型
	private LinearLayout           head;
	private View                   tail;
	private LinearLayout           llhideHead;
	private int                    llhideHead_measuredHeight;
	private int                    tail_measuredHeight;
	private float                  downY = -1;
	private View                   lunboView_to_find_location;
	private int                    lunboView_Onscreen_locationY;
	private final int              PULL_DOWN = 1;// 下拉状态
	private final int              RELEASE_STATE = 3; // 松开状态
	private final int              REFRESHING = 5;// 正在刷新
	private int                    currentState=PULL_DOWN;//默认是下拉刷新状态  ？？？？
	private TextView               tv_head_textState;
	private TextView               tv_text_refresh_time;
	private ImageView              iv_red_arrow;
	private ProgressBar            pb_jindutiao;
	private RotateAnimation        ra_up;
	private RotateAnimation        ra_down;
	private OnRefreshDataListener  listener;
	private boolean                isEnablePullRefresh=false;
	private boolean                isLoadingMore=false;//是否加载更多数据
	//private boolean                isAddMoreData=false;
	//构造函数1
	public RefreshHeadTailListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initView();
		//初始化动画   执行一次  所以 放在这
		initAnimation();
		
		// add scroll event for load more
		initEvent();
		
	}
	private void initEvent() {
		//为listview添加滑动事件
		setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if(!isLoadingMore){
					return;
				}
				//加一个 &&是为了 只 加载更多 一次   //
				//System.out.println("当前位置"+getLastVisiblePosition()+"  总共"+getAdapter().getCount());
				if(getLastVisiblePosition()==getAdapter().getCount()-1&&!isLoadingMore){//因为visibleposition是从0开始的
					//如果滑动到最后一条数据   则显示加载更多数据
					//显示 加载更多数据的视图
					tail.setPadding(0,0, 0, 0);
					setSelection(getAdapter().getCount());
					//System.out.println("加载更多数据》》》》》");
					//真正的去加载更多的数据
					if(listener!=null){
						//改变下布尔变量的状态
						isLoadingMore=true;
						listener.loadingMore();
					}					
				}
			 
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	//构造函数2
	public RefreshHeadTailListView(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}
	//构造函数3
	public RefreshHeadTailListView(Context context) {
		this(context,null);
	}
	private void initView() {
		// TODO Auto-generated method stub
		initHead();
		initTail();
	}
   //这个方法 按视频上的来用  没有效果 --！  
	public  void addLunboView(View view){
		head.addView(view);
		
	}
	//由用户决定是否启用下拉刷新  若用则true 否则false
	public void isUsePullRefresh(boolean ispullrefresh){
		isEnablePullRefresh=ispullrefresh;
	}
	
	//由用户选择是否 启用 加载更多的功能 
	public void isLoadMoreData(boolean isloadingmoredata){
		isLoadingMore = isloadingmoredata;
	}
	
	//重写addHeaderView方法
	@Override
	public void addHeaderView(View view) {
		//如果使用下拉刷新 
		if(isEnablePullRefresh){
			lunboView_to_find_location = view;			
			head.addView(view);
		}else{
			//若不使用  则使用listview的原生加载方法
			super.addHeaderView(view);
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.AbsListView#onTouchEvent(android.view.MotionEvent)
	 * 将隐藏的刷新头和刷新尾 拉出来
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		  switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if(!isEnablePullRefresh){
				//if we don't use pull refresh
				//just  break
				break;
			}
			if(currentState==REFRESHING){
				break;
			}
			// 获取listview与轮播图在屏幕中的坐标，来判断轮播图是否完全显示
			if(!isLunboFullShow()){
				//如果没有完全显示
				//直接break掉，调用listvIEW的响应事件
				break;
			}

            if(downY==-1){//防止按下的时候没有获取到这个坐标
            	downY = ev.getY();
            }
			
			float moveY = ev.getY();
			// 当下拉拖动时，处理自己的事件，不让listview的原生事件生效
			
			float dy = moveY - downY;
			//dy表示拖动的距离
			if (dy > 0 && getFirstVisiblePosition() == 0) {
				// 拖动了多少 上方就露出来多少
				float scrollYDis = -llhideHead_measuredHeight + dy;
				if (scrollYDis < 0&&currentState!=PULL_DOWN) {
					// 此时 刷新头还没有完全的显示出来
				   currentState=PULL_DOWN;
                   refreshState();
				} else if(scrollYDis>=0&&currentState!=RELEASE_STATE){
					currentState=RELEASE_STATE;
				   refreshState();
				}
				llhideHead.setPadding(0, (int) scrollYDis, 0, 0);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:   ///松开时的操作
			downY=-1;
			if(currentState==PULL_DOWN){
			    llhideHead.setPadding(0, -llhideHead_measuredHeight, 0, 0);
			}else if(currentState==RELEASE_STATE){
				//此时刷新数据
				llhideHead.setPadding(0, 0, 0, 0);
				currentState=REFRESHING;
				refreshState();
				
			}
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	//箭头的旋转动画
	private void initAnimation(){
		ra_up = new RotateAnimation(0, -180, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		ra_up.setDuration(500);
		ra_up.setFillAfter(true);
		ra_down = new RotateAnimation(-180, -360, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		ra_up.setDuration(500);
		ra_up.setFillAfter(true);
	}
	private void refreshState() {
		switch (currentState) {
		case PULL_DOWN:
			
			tv_head_textState.setText("下拉刷新");
			//下拉刷新应选择 向上的动画
			iv_red_arrow.startAnimation(ra_down);
			break;
		case RELEASE_STATE:
			
			tv_head_textState.setText("松开刷新");
			iv_red_arrow.startAnimation(ra_up);
			break;
		case REFRESHING:
			iv_red_arrow.clearAnimation();
			iv_red_arrow.setVisibility(View.GONE);//隐藏箭头
			pb_jindutiao.setVisibility(View.VISIBLE);//显示进度条
			tv_head_textState.setText("正在刷新数据");
			//是时候刷新真正的数据了！(认真脸)
			if(listener!=null){
				listener.refreshData();
			}
			
		default:
			break;
		}
		
	}
	//定义一个接口，让新闻主界面那个类实现这个接口     懵懂~~
	public interface OnRefreshDataListener{
		//该方法是让新闻主界面那个类来实现的~
		void refreshData();
		//再定义一个接口中的方法 来在listview的底部 加载更多的数据
		void loadingMore();
	}
	public void setOnRefreshDataListener(OnRefreshDataListener listener){
		this.listener=listener;
	}
	//刷新状态完成
	public void refreshStateFinish(){
		
		if (isLoadingMore) {
			isLoadingMore=false;
			tail.setPadding(0, -tail_measuredHeight, 0, 0);
			
			

		} else {
			tv_head_textState.setText("下拉刷新");
			// 下拉刷新应选择 向上的动画
			// iv_red_arrow.startAnimation(ra_down);
			iv_red_arrow.setVisibility(View.VISIBLE);// 初始化箭头
			pb_jindutiao.setVisibility(View.INVISIBLE);// 隐藏进度条
			// 设置刷新时间为当前 的时间
			tv_text_refresh_time.setText(getCurrentFormatDate());
			// 隐藏头布局
			llhideHead.setPadding(0, -llhideHead_measuredHeight, 0, 0);
			// state value must be initialed too
			currentState = PULL_DOWN;
		}
	}
	private String getCurrentFormatDate(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH-mm-ss");
		return format.format(new Date());
	}
	private Boolean isLunboFullShow() {
		//如果没设置 轮播图
		if(lunboView_to_find_location==null){
			return true;
		}
		//获取listview与轮播图在屏幕中的坐标，来判断轮播图是否完全显示  
		int location[] = new int[2];
		// listview在屏幕中的坐标
		if(lunboView_Onscreen_locationY==0){							
		   this.getLocationOnScreen(location);
		   //获取一下listView的Y轴高度
		   lunboView_Onscreen_locationY=location[1];
		}
		
		// 轮播图在屏幕中的坐标

		lunboView_to_find_location.getLocationOnScreen(location);

		if(location[1]<lunboView_Onscreen_locationY){
			//说明 轮播图还没有完全显示
			//则继续响应listView的时间			
			return false;
		}
		return true;
	}
	//初始化头部
	private void initHead(){
		head = (LinearLayout) View.inflate(getContext(), R.layout.head_refresh_listview, null);
		
		llhideHead = (LinearLayout) head.findViewById(R.id.ll_head_listView_root);
		tv_head_textState = (TextView) head.findViewById(R.id.desc_listview_refresh);
		tv_text_refresh_time = (TextView) head.findViewById(R.id.desc_listview_refresh_time);
		iv_red_arrow = (ImageView) head.findViewById(R.id.red_arrow);
		pb_jindutiao = (ProgressBar) head.findViewById(R.id.jindutiao);
		//隐藏刷新头的根布局
		//获得刷新头组件的高度
		llhideHead.measure(0, 0);//工具方法 让其自己去测量
		llhideHead_measuredHeight = llhideHead.getMeasuredHeight();
		//隐藏刷新头到上面  坐标应为 负
		head.setPadding(0, -llhideHead_measuredHeight, 0, 0);
		addHeaderView(head);
	}
	//初始化尾部
    private void initTail(){
		tail = View.inflate(getContext(), R.layout.tail_refresh_listview, null);
		tail.measure(0, 0);
		tail_measuredHeight = tail.getMeasuredHeight();
		tail.setPadding(0, -tail_measuredHeight, 0, 0);
		
		addFooterView(tail);
	}

}
